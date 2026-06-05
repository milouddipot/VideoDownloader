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
        connection.instanceFollowRedirects = true

        connection.setRequestProperty("User-Agent",
            "com.google.android.youtube/19.09.37 (Linux; U; Android 13; Pixel 7 Build/TQ3A.230901.001) gzip")
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9")
        connection.setRequestProperty("Accept", "*/*")
        connection.setRequestProperty("X-YouTube-Client-Name", "3")
        connection.setRequestProperty("X-YouTube-Client-Version", "19.09.37")

        request.headers().forEach { (key, values) ->
            values.forEach { value -> connection.setRequestProperty(key, value) }
        }

        val body = request.dataToSend()
        if (body != null) {
            connection.doOutput = true
            connection.outputStream.write(body)
        }

        val responseCode = connection.responseCode
        val responseBody = try {
            connection.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            connection.errorStream?.bufferedReader()?.readText() ?: ""
        }

        val responseHeaders = connection.headerFields
            .filter { it.key != null }
            .mapValues { it.value }

        return Response(responseCode, connection.responseMessage,
            responseHeaders, responseBody, request.url())
    }
}
