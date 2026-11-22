# Hướng dẫn chạy Web UI + kết nối với backend Java

## 1. Kiến trúc kết nối

```
Trình duyệt (web/index.html, style.css, app.js)
        │  HTTP/JSON  (fetch → /api/...)
        ▼
ApiServer.java  (com.sun.net.httpserver – có sẵn trong JDK, KHÔNG cần Maven/Spring)
        │  gọi trực tiếp
        ▼
SocialGraph  +  SocialAnalyzer   (code thuật toán của bạn)
```

`ApiServer` vừa phục vụ file giao diện (thư mục `web/`) vừa cung cấp REST API, nên **chỉ cần chạy 1 lệnh** và mở `http://localhost:8080`. Không bị lỗi CORS vì cùng một origin.

## 2. Yêu cầu
- JDK 11 trở lên (kiểm tra: `java -version`, `javac -version`).

## 3. Các bước chạy

### Windows
1. Mở thư mục dự án, nhấp đúp `run-web.bat` (hoặc chạy trong CMD: `run-web.bat`).
2. Trình duyệt tự mở `http://localhost:8080`. Nếu không, tự mở địa chỉ này.
3. Dừng server: `Ctrl + C` trong cửa sổ lệnh.

### Linux / WSL / macOS
```bash
./run-web.sh            # cổng mặc định 8080
./run-web.sh 9090       # đổi cổng
```

### Chạy thủ công
```bash
javac -encoding UTF-8 -d bin $(find src -name '*.java')
java -cp bin ptit.dsa.socialnetwork.web.ApiServer 8080 web
```
(Windows CMD: `dir /s /b src\*.java > sources.txt` rồi `javac -encoding UTF-8 -d bin @sources.txt`.)

## 4. Danh sách REST API

| Method | Đường dẫn | Chức năng |
|---|---|---|
| GET | `/api/graph` | Toàn bộ đồ thị + thống kê |
| GET | `/api/mutual?a=U01&b=U02` | Bạn chung + Jaccard |
| GET | `/api/recommend?user=U01&algo=bfs\|dfs&depth=3&limit=5` | Gợi ý bạn mới |
| GET | `/api/path?from=U01&to=U10` | Đường ngắn nhất (BFS) |
| GET | `/api/influencers?k=5` | Top người có nhiều bạn nhất |
| POST | `/api/users` | Thêm người dùng |
| DELETE | `/api/users/{id}` | Xóa người dùng |
| POST / DELETE | `/api/friendships` (a, b) | Kết bạn / hủy kết bạn |
| POST | `/api/reset` | Nạp lại dữ liệu mẫu |

Thử nhanh bằng terminal: `curl "http://localhost:8080/api/path?from=U01&to=U10"`

## 5. Chạy frontend riêng (VS Code Live Server – tùy chọn)
Giữ backend chạy ở cổng 8080, mở `web/index.html` bằng Live Server (cổng 5500). `app.js` đã tự trỏ API về `http://localhost:8080`, và backend đã bật CORS.

## 6. Cách chụp ảnh màn hình cho báo cáo
Chạy server, mở `http://localhost:8080`, bấm từng tab rồi chụp (Win + Shift + S):
1. Màn hình chính (đồ thị + thống kê)
2. Tab **Bạn chung** → "Tìm bạn chung"
3. Tab **Gợi ý** → BFS, rồi DFS
4. Tab **Đường ngắn nhất** → U01 → U10
5. Tab **Quản lý** (thêm người dùng / kết bạn)

Ảnh mẫu đã chụp sẵn trong `docs/screenshots/` (giao diện nền sáng, 2880×1800 px – in rõ nét).
Ảnh giao diện nền tối cũ được giữ lại trong `docs/screenshots_cu_nen_toi/`.

**Chụp lại tự động:** khi server đang chạy, chạy `bash chup-anh-bao-cao.sh` (dùng Microsoft Edge chế độ headless).
Có thể mở trực tiếp từng màn hình bằng tham số `?demo=`:
`http://localhost:8080/?demo=mutual` · `?demo=bfs` · `?demo=dfs` · `?demo=path` · `?demo=manage`.

**Ảnh phóng to từng vùng (chữ to khi in):** khi server đang chạy, chạy `python chup_anh_phong_to.py` (cần Pillow). Script chụp lại ảnh toàn màn hình và cắt riêng bảng điều khiển, đồ thị, bảng kết quả vào `docs/screenshots/zoom/`.

**Mẹo chèn ảnh vào Word:** đặt ảnh rộng hết lề trang (khoảng 16–17 cm), hoặc xoay trang ngang cho phần phụ lục ảnh để chữ trong ảnh to hơn.

## 7. Lỗi thường gặp
| Lỗi | Cách xử lý |
|---|---|
| `Address already in use` | Cổng bận → `./run-web.sh 9090` |
| Giao diện báo "Không kết nối được backend" | Chưa chạy server, hoặc mở sai cổng |
| Tiếng Việt bị lỗi dấu trong console | Dùng `run-web.bat` (đã `chcp 65001`) |
| `javac` không nhận | Cài JDK và thêm `bin` vào PATH |
