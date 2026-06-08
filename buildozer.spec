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
android.gradle_dependencies = 
android.enable_androidx = True
android.accept_sdk_license = True
android.sdk = 33
android.build_tools_version = 33.0.2
android.gradle = 7.5
fullscreen = 0

[buildozer]
log_level = 2
