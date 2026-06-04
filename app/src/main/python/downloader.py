import yt_dlp
import os

def download(url, format_type, output_dir):
    try:
        if format_type == "mp3":
            ydl_opts = {
                'format': 'bestaudio/best',
                'outtmpl': os.path.join(output_dir, '%(title)s.%(ext)s'),
                'quiet': True,
            }
        else:
            ydl_opts = {
                'format': 'best[ext=mp4]/best',
                'outtmpl': os.path.join(output_dir, '%(title)s.%(ext)s'),
                'quiet': True,
            }

        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=True)
            title = info.get('title', 'الفيديو')
            return f"اكتمل التحميل: {title}"

    except Exception as e:
        return f"خطأ: {str(e)}"
