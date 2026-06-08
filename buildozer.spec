[app]
title = YT Downloader
package.name = ytdownloader
package.domain = com.videodownloader
source.dir = .
source.include_exts = py
version = 1.0
requirements = python3,kivy,yt-dlp
orientation = portrait
android.permissions = INTERNET, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE
android.api = 33
android.minapi = 26
android.archs = arm64-v8a
fullscreen = 0

[buildozer]
log_level = 2
