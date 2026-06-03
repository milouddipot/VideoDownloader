package com.videodownloader.app

import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var urlEditText: TextInputEditText
    private lateinit var btnMp4: Button
    private lateinit var btnMp3: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        urlEditText = findViewById(R.id.urlEditText)
        btnMp4 = findViewById(R.id.btnMp4)
        btnMp3 = findViewById(R.id.btnMp3)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)

        intent?.getStringExtra(android.content.Intent.EXTRA_TEXT)?.let {
            urlEditText.setText(it)
        }

        btnMp4.setOnClickListener { startDownload("mp4") }
        btnMp3.setOnClickListener { startDownload("mp3") }
    }

    private fun startDownload(format: String) {
        val url = urlEditText.text.toString().trim()
        if (url.isEmpty()) {
            showStatus("الرجاء إدخال رابط الفيديو")
            return
        }

        setDownloading(true)
        showStatus(getString(R.string.downloading))

        thread {
            try {
                val py = Python.getInstance()
                val module = py.getModule("downloader")
                val outputDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ).absolutePath

                val result = module.callAttr("download", url, format, outputDir).toString()

                runOnUiThread {
                    setDownloading(false)
                    showStatus(result)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    setDownloading(false)
                    showStatus("${getString(R.string.download_error)}: ${e.message}")
                }
            }
        }
    }

    private fun setDownloading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnMp4.isEnabled = !loading
        btnMp3.isEnabled = !loading
    }

    private fun showStatus(message: String) {
        statusText.visibility = View.VISIBLE
        statusText.text = message
    }
}
