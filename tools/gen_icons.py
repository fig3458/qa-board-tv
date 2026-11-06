from PIL import Image, ImageDraw, ImageFont
import os

base = r"C:\Users\Administrator\Projects\qa-board-tv\app\src\main\res"
sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def load_font(size: int):
    for fp in [
        r"C:\Windows\Fonts\arialbd.ttf",
        r"C:\Windows\Fonts\segoeuib.ttf",
        r"C:\Windows\Fonts\msyhbd.ttc",
        r"C:\Windows\Fonts\arial.ttf",
    ]:
        if os.path.exists(fp):
            try:
                return ImageFont.truetype(fp, size=size)
            except Exception:
                pass
    return ImageFont.load_default()


def make_icon(size: int, path: str):
    img = Image.new("RGBA", (size, size), (11, 18, 32, 255))
    draw = ImageDraw.Draw(img)
    margin = max(2, size // 12)
    draw.rounded_rectangle(
        [margin, margin, size - margin - 1, size - margin - 1],
        radius=max(4, size // 8),
        fill=(47, 107, 255, 255),
    )
    font = load_font(int(size * 0.42))
    text = "QA"
    bbox = draw.textbbox((0, 0), text, font=font)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    x = (size - tw) / 2 - bbox[0]
    y = (size - th) / 2 - bbox[1] - size * 0.02
    draw.text((x, y), text, fill=(255, 255, 255, 255), font=font)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, "PNG")


for folder, size in sizes.items():
    make_icon(size, os.path.join(base, folder, "ic_launcher.png"))

banner = Image.new("RGBA", (320, 180), (11, 18, 32, 255))
d = ImageDraw.Draw(banner)
d.rounded_rectangle([90, 40, 230, 140], radius=16, fill=(47, 107, 255, 255))
font = load_font(64)
text = "QA"
bbox = d.textbbox((0, 0), text, font=font)
tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
d.text(
    ((320 - tw) / 2 - bbox[0], (180 - th) / 2 - bbox[1] - 4),
    text,
    fill="white",
    font=font,
)
banner_dir = os.path.join(base, "drawable-xhdpi")
os.makedirs(banner_dir, exist_ok=True)
banner.save(os.path.join(banner_dir, "tv_banner.png"), "PNG")
print("icons ok")
