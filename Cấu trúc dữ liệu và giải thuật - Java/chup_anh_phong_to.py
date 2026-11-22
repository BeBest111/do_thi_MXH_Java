"""Chụp ảnh giao diện cho báo cáo: ảnh toàn màn hình + ảnh phóng to từng vùng.
Yêu cầu: server đang chạy ở http://localhost:8080 (run-web.bat), Microsoft Edge, Pillow.
Chạy:  python chup_anh_phong_to.py
Kết quả: docs/screenshots/*.png  (ảnh toàn màn hình)  và  docs/screenshots/zoom/*.png (ảnh cắt phóng to)
"""
import html, json, os, re, subprocess, tempfile
from PIL import Image

EDGE = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
BASE = "http://localhost:8080/"
SCALE = 2.25
ROOT = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(ROOT, "docs", "screenshots")
ZOOM = os.path.join(OUT, "zoom")
os.makedirs(ZOOM, exist_ok=True)

# (tên file, tham số demo, chiều cao khung thường, các vùng cần phóng to)
SCREENS = [
    ("01_main", "", 880, ["stage", "results"]),
    ("02_mutual", "mutual", 880, ["controls", "stage", "results"]),
    ("03_recommend_bfs", "bfs", 880, ["controls", "stage", "results"]),
    ("04_recommend_dfs", "dfs", 880, ["controls", "stage", "results"]),
    ("05_path", "path", 880, ["controls", "stage", "results"]),
    ("06_manage", "manage", 1060, ["controls"]),
]


def edge(args, height, width=1280):
    prof = tempfile.mkdtemp()
    cmd = [EDGE, "--headless=new", "--disable-gpu", "--hide-scrollbars", f"--user-data-dir={prof}",
           f"--window-size={width},{height}", f"--force-device-scale-factor={SCALE}",
           "--virtual-time-budget=10000"] + args
    return subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace").stdout


def capture(url, height, png):
    """Chụp ảnh và trả về tọa độ (px CSS) các vùng giao diện."""
    edge([f"--screenshot={png}", url], height)
    # Chế độ --dump-dom có khung nhìn nhỏ hơn chế độ chụp ảnh -> tự bù cho bằng nhau
    want = (Image.open(png).width / SCALE, Image.open(png).height / SCALE)
    w, h = 1280, height
    for _ in range(3):
        dom = edge(["--dump-dom", url], h, w)
        rects = json.loads(html.unescape(re.search(r'data-rects="([^"]*)"', dom).group(1)))
        dw, dh = want[0] - rects["vw"][0], want[1] - rects["vw"][1]
        if abs(dw) < 1 and abs(dh) < 1:
            return rects
        w, h = round(w + dw), round(h + dh)
    raise RuntimeError(f"Khung nhin khong khop: {rects['vw']} != {want}")


def crop(png, rect, dst, pad=6):
    im = Image.open(png)
    l, t, r, b = [round(v * SCALE) for v in rect]
    p = round(pad * SCALE)
    im.crop((max(0, l - p), max(0, t - p), min(im.width, r + p), min(im.height, b + p))).save(dst, optimize=True)


for name, demo, h, zones in SCREENS:
    q = f"?demo={demo}&" if demo else "?"
    full = os.path.join(OUT, name + ".png")
    rects = capture(BASE + q + "rects=1", h, full)            # ảnh tổng quan
    print("Da chup", name)
    if "stage" in zones:                                         # đồ thị lấy từ ảnh tổng quan
        crop(full, rects["stage"], os.path.join(ZOOM, f"{name}_dothi.png"))
    side = [z for z in zones if z != "stage"]
    if side:                                                     # bảng trái/phải: khung cao, không bị cắt
        tall = os.path.join(tempfile.mkdtemp(), "tall.png")
        rt = capture(BASE + q + "rects=tall", 1700, tall)
        for z in side:
            label = {"controls": "dieukhien", "results": "ketqua"}[z]
            crop(tall, rt[z], os.path.join(ZOOM, f"{name}_{label}.png"))
    print("  -> phong to:", ", ".join(zones))
