package com.videodownloader.app

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.VideoStream
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var urlEditText: TextInputEditText
    private lateinit var btnFetch: Button
    private lateinit var btnMp4: Button
    private lateinit var btnMp3: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var videoTitle: TextView

    private var videoStreams: List<VideoStream> = emptyList()
    private var audioUrl: String = ""
    private var currentTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NewPipe.init(DownloaderImpl.getInstance())

        urlEditText = findViewById(R.id.urlEditText)
        btnFetch = findViewById(R.id.btnFetch)
        btnMp4 = findViewById(R.id.btnMp4)
        btnMp3 = findViewById(R.id.btnMp3)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        videoTitle = findViewById(R.id.videoTitle)

        intent?.getStringExtra(android.content.Intent.EXTRA_TEXT)?.let {
            urlEditText.setText(it)
        }

        btnFetch.setOnClickListener { fetchVideoInfo() }
        btnMp4.setOnClickListener { downloadVideo("mp4") }
        btnMp3.setOnClickListener { downloadVideo("mp3") }
    }

    private fun fetchVideoInfo() {
        val url = urlEditText.text.toString().trim()
        if (url.isEmpty()) { showStatus("الرجاء إدخال رابط"); return }

        setLoading(true)
        showStatus("جاري جلب معلومات الفيديو...")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val extractor = ServiceList.YouTube.getStreamExtractor(url)
                extractor.fetchPage()
                currentTitle = extractor.name
                videoStreams = extractor.videoStreams
                audioUrl = extractor.audioStreams.firstOrNull()?.url ?: ""

                withContext(Dispatchers.Main) {
                    setLoading(false)
                    videoTitle.text = currentTitle
                    videoTitle.visibility = View.VISIBLE
                    btnMp4.visibility = View.VISIBLE
                    btnMp3.visibility = View.VISIBLE
                    showStatus("جاهز للتحميل ✅")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    showStatus("خطأ: ${e.message}")
                }
            }
        }
    }

    private fun downloadVideo(format: String) {
        setLoading(true)
        showStatus("جاري التحميل...")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val downloadUrl = if (format == "mp3") {
                    audioUrl
                } else {
                    videoStreams.firstOrNull { it.resolution.contains("720") }?.url
                        ?: videoStreams.firstOrNull()?.url ?: ""
                }

                if (downloadUrl.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                        showStatus("خطأ: لم يتم العثور على رابط التحميل")
                    }
                    return@launch
                }

                val ext = if (format == "mp3") "m4a" else "mp4"
                val fileName = "${currentTitle.take(50)}.${ext}"
                val outputDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                outputDir.mkdirs()
                val outputFile = java.io.File(outputDir, fileName)

                val connection = URL(downloadUrl).openConnection()
                connection.connect()
                val input = BufferedInputStream(connection.getInputStream())
                val output = FileOutputStream(outputFile)
                val buffer = ByteArray(8192)
                var count: Int
                while (input.read(buffer).also { count = it } != -1) {
                    output.write(buffer, 0, count)
                }
                output.flush()
                output.close()
                input.close()

                withContext(Dispatchers.Main) {
                    setLoading(false)
                    showStatus("اكتمل التحميل ✅\n${outputFile.absolutePath}")
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
        btnFetch.isEnabled = !loading
        btnMp4.isEnabled = !loading
        btnMp3.isEnabled = !loading
    }

    private fun showStatus(message: String) {
        statusText.visibility = View.VISIBLE
        statusText.text = message
    }
}
