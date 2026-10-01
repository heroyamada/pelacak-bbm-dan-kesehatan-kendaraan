from PIL import Image
import os

image_path = r"C:\Users\MSI KATANA\.gemini\antigravity-ide\brain\7d6ef89e-5193-4b1e-854a-d0f9c9f36a71\vehicle_tracker_logo_1790861643732.jpg"
res_dir = r"app\src\main\res"

sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192
}

if not os.path.exists(image_path):
    print("Image not found:", image_path)
    exit(1)

img = Image.open(image_path)

for folder, size in sizes.items():
    folder_path = os.path.join(res_dir, folder)
    if os.path.exists(folder_path):
        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        # overwrite ic_launcher.webp
        resized.save(os.path.join(folder_path, "ic_launcher.webp"), format="WEBP")
        # overwrite ic_launcher_round.webp (create a rounded mask if needed, but a square is fine if we just overwrite it, or we can crop it to circle)
        
        # create circular mask for round icon
        mask = Image.new('L', (size, size), 0)
        from PIL import ImageDraw
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0, size, size), fill=255)
        
        round_img = resized.copy()
        round_img.putalpha(mask)
        round_img.save(os.path.join(folder_path, "ic_launcher_round.webp"), format="WEBP")
        
        # if there are pngs, overwrite them too just in case
        if os.path.exists(os.path.join(folder_path, "ic_launcher.png")):
            resized.save(os.path.join(folder_path, "ic_launcher.png"), format="PNG")
        if os.path.exists(os.path.join(folder_path, "ic_launcher_round.png")):
            round_img.save(os.path.join(folder_path, "ic_launcher_round.png"), format="PNG")

# Also delete mipmap-anydpi-v26 to force fallback to the standard ones we just replaced, so we don't have to deal with vector XMLs
import shutil
anydpi = os.path.join(res_dir, "mipmap-anydpi-v26")
if os.path.exists(anydpi):
    shutil.rmtree(anydpi)
anydpi33 = os.path.join(res_dir, "mipmap-anydpi-v33")
if os.path.exists(anydpi33):
    shutil.rmtree(anydpi33)

print("Icons replaced successfully!")
