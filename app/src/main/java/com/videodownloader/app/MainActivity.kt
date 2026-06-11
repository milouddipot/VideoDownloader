package com.videodownloader.app

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var urlEditText: TextInputEditText
    private lateinit var btnMp4: Button
    private lateinit var btnMp3: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var ytDlpPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlEditText = findViewById(R.id.urlEditText)
        btnMp4 = findViewById(R.id.btnMp4)
        btnMp3 = findViewById(R.id.btnMp3)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)

        ytDlpPath = setupYtDlp()

        intent?.getStringExtra(android.content.Intent.EXTRA_TEXT)?.let {
            urlEditText.setText(it)
        }

        btnMp4.setOnClickListener { startDownload("mp4") }
        btnMp3.setOnClickListener { startDownload("mp3") }
    }

    private fun setupYtDlp(): String {
        val ytDlpFile = File(applicationInfo.nativeLibraryDir).parentFile
            ?.let { File(it, "yt-dlp") }
            ?: File(filesDir, "yt-dlp")

        val ytDlpInCache = File(cacheDir, "yt-dlp")

        if (!ytDlpInCache.exists()) {
            assets.open("yt-dlp").use { input ->
                ytDlpInCache.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        ytDlpInCache.setExecutable(true, false)
        ytDlpInCache.setReadable(true, false)

        return ytDlpInCache.absolutePath
    }

    private fun startDownload(format: String) {
        val url = urlEditText.text.toString().trim()
        if (url.isEmpty()) {
            showStatus("الرجاء إدخال رابط")
            return
        }

        setLoading(true)
        showStatus("جاري التحميل...")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val outputDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ).absolutePath

                val formatStr = if (format == "mp3") {
                    "bestaudio[ext=m4a]/bestaudio/best"
                } else {
                    "best[ext=mp4]/best"
                }

                val process = ProcessBuilder(
                    ytDlpPath,
                    "--format", formatStr,
                    "--output", "$outputDir/%(title).80s.%(ext)s",
                    "--no-playlist",
                    "--retries", "5",
                    "--user-agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 Chrome/121.0.0.0 Mobile Safari/537.36",
                    url
                )
                    .redirectErrorStream(true)
                    .start()

                val output = process.inputStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                withContext(Dispatchers.Main) {
                    setLoading(false)
                    if (exitCode == 0) {
                        showStatus("اكتمل التحميل ✅")
                    } else {
                        val error = output.lines()
                            .filter { it.contains("ERROR", ignoreCase = true) }
                            .lastOrNull() ?: output.takeLast(200)
                        showStatus("خطأ: $error")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    showStatus("خطأ: ${e.message}")
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnMp4.isEnabled = !loading
        btnMp3.isEnabled = !loading
    }

    private fun showStatus(message: String) {
        statusText.visibility = View.VISIBLE
        statusText.text = message
    }
}
