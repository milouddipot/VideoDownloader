package com.videodownloader.app

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.net.HttpURLConnection
import java.net.URL

class DownloaderImpl private constructor() : Downloader() {

    companion object {
        private var instance: DownloaderImpl? = null
        fun getInstance(): DownloaderImpl {
            if (instance == null) instance = DownloaderImpl()
            return instance!!
        }
    }

    override fun execute(request: Request): Response {
        val url = URL(request.url())
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = request.httpMethod()
        connection.connectTimeout = 30000
        connection.readTimeout = 30000
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 Chrome/121.0.0.0 Mobile Safari/537.36"
        )

        request.headers().forEach { (key, values) ->
            values.forEach { value -> connection.setRequestProperty(key, value) }
        }

        val responseCode = connection.responseCode
        val responseBody = connection.inputStream.bufferedReader().readText()
        val responseHeaders = connection.headerFields
            .filter { it.key != null }
            .mapValues { it.value }

        return Response(responseCode, connection.responseMessage, responseHeaders, responseBody, request.url())
    }
}
