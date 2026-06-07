import os
import sys
import subprocess
import re

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/121.0.0.0 Mobile Safari/537.36"
    )
}

def ensure_ytdlp():
    try:
        import yt_dlp
        return True
    except ImportError:
        try:
            subprocess.run(
                [sys.executable, "-m", "pip", "install", "yt-dlp", "--quiet"],
                capture_output=True,
                timeout=60
            )
            return True
        except Exception as e:
            return False

def download(url, format_type, output_dir):
    try:
        if not ensure_ytdlp():
            return "خطأ: فشل تثبيت yt-dlp، تحقق من الإنترنت"

        import yt_dlp
        os.makedirs(output_dir, exist_ok=True)

        if format_type == "mp3":
            format_str = "bestaudio[ext=m4a]/bestaudio/best"
        else:
            format_str = "best[ext=mp4]/best"

        ydl_opts = {
            "format": format_str,
            "outtmpl": os.path.join(output_dir, "%(title).80s.%(ext)s"),
            "noplaylist": True,
            "quiet": True,
            "no_warnings": True,
            "retries": 5,
            "fragment_retries": 5,
            "http_headers": HEADERS,
        }

        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=True)
            title = info.get("title", "الفيديو")[:80]
            return f"اكتمل التحميل ✅: {title}"

    except Exception as e:
        err = re.sub(r"\x1b\[[0-9;]*m", "", str(e)).strip()
        if "ffmpeg" in err.lower():
            return "خطأ: ffmpeg غير مثبت"
        elif "private" in err.lower() or "login" in err.lower():
            return "خطأ: الفيديو خاص أو يتطلب تسجيل دخول"
        elif "not available" in err.lower():
            return "خطأ: الفيديو غير متاح"
        return f"خطأ: {err[:200]}"
