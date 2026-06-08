#!/usr/bin/env python3
import os
import sys
import subprocess
import importlib.util
import re

DOWNLOAD_DIR = "/storage/emulated/0/Download/VideoDownloader"

PLATFORM_PATTERNS = {
    "YouTube":   r"(youtube\.com|youtu\.be)",
    "Instagram": r"instagram\.com",
    "TikTok":    r"(tiktok\.com|vm\.tiktok\.com)",
    "Facebook":  r"(facebook\.com|fb\.watch|fb\.com)",
}

def ensure_yt_dlp():
    if importlib.util.find_spec("yt_dlp") is None:
        subprocess.check_call([sys.executable, "-m", "pip", "install", "--quiet", "yt-dlp"])

def detect_platform(url):
    for platform, pattern in PLATFORM_PATTERNS.items():
        if re.search(pattern, url, re.IGNORECASE):
            return platform
    return "Unknown"

def download_video(url):
    import yt_dlp
    os.makedirs(DOWNLOAD_DIR, exist_ok=True)
    ydl_opts = {
        "format": "best[ext=mp4]/best",
        "outtmpl": os.path.join(DOWNLOAD_DIR, "%(title).80s.%(ext)s"),
        "noplaylist": True,
        "quiet": False,
        "retries": 5,
        "http_headers": {
            "User-Agent": (
                "Mozilla/5.0 (Linux; Android 13; Pixel 7) "
                "AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/121.0.0.0 Mobile Safari/537.36"
            )
        },
    }
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(url, download=True)
        return info.get("title", "Done")

from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.textinput import TextInput
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.core.window import Window
import threading

Window.clearcolor = (0.06, 0.06, 0.06, 1)

class MainLayout(BoxLayout):
    def __init__(self, **kwargs):
        super().__init__(orientation='vertical', padding=24, spacing=12, **kwargs)

        self.add_widget(Label(
            text='YT Downloader',
            font_size='28sp',
            bold=True,
            color=(1, 0, 0, 1),
            size_hint_y=None,
            height=60
        ))

        self.url_input = TextInput(
            hint_text='الصق رابط الفيديو هنا',
            multiline=False,
            size_hint_y=None,
            height=50,
            background_color=(0.15, 0.15, 0.15, 1),
            foreground_color=(1, 1, 1, 1)
        )
        self.add_widget(self.url_input)

        btn_mp4 = Button(
            text='تحميل MP4',
            size_hint_y=None,
            height=56,
            background_color=(1, 0, 0, 1)
        )
        btn_mp4.bind(on_press=lambda x: self.start_download('mp4'))
        self.add_widget(btn_mp4)

        btn_mp3 = Button(
            text='تحميل MP3',
            size_hint_y=None,
            height=56,
            background_color=(0.13, 0.13, 0.13, 1)
        )
        btn_mp3.bind(on_press=lambda x: self.start_download('mp3'))
        self.add_widget(btn_mp3)

        self.status = Label(
            text='',
            color=(1, 1, 1, 1),
            font_size='14sp',
            text_size=(Window.width - 48, None),
            halign='center'
        )
        self.add_widget(self.status)

    def start_download(self, fmt):
        url = self.url_input.text.strip()
        if not url:
            self.status.text = 'الرجاء إدخال رابط'
            return
        self.status.text = 'جاري التحميل...'
        threading.Thread(target=self.do_download, args=(url, fmt), daemon=True).start()

    def do_download(self, url, fmt):
        try:
            ensure_yt_dlp()
            import yt_dlp
            os.makedirs(DOWNLOAD_DIR, exist_ok=True)
            if fmt == 'mp3':
                format_str = 'bestaudio[ext=m4a]/bestaudio/best'
            else:
                format_str = 'best[ext=mp4]/best'
            ydl_opts = {
                'format': format_str,
                'outtmpl': os.path.join(DOWNLOAD_DIR, '%(title).80s.%(ext)s'),
                'noplaylist': True,
                'quiet': True,
                'retries': 5,
                'http_headers': {
                    'User-Agent': 'Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 Chrome/121.0.0.0 Mobile Safari/537.36'
                },
            }
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(url, download=True)
                title = info.get('title', 'الفيديو')[:60]
            self.status.text = f'اكتمل التحميل ✅\n{title}'
        except Exception as e:
            self.status.text = f'خطأ: {str(e)[:150]}'

class VideoDownloaderApp(App):
    def build(self):
        return MainLayout()

if __name__ == '__main__':
    ensure_yt_dlp()
    VideoDownloaderApp().run()
