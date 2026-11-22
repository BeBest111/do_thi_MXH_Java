# BÀI TẬP LỚN: PHÂN TÍCH MẠNG XÃ HỘI BẰNG ĐỒ THỊ (GRAPH TRAVERSAL)

- **Học viện**: Học viện Công nghệ Bưu chính Viễn thông (PTIT)
- **Khoa**: Công nghệ Thông tin
- **Bộ môn**: Cấu trúc dữ liệu và giải thuật - Java
- **Giảng viên hướng dẫn**: ThS. Vũ Đình Tân
- **Nhóm thực hiện**: Nhóm 12 - Lớp D22VHCNPM02
  1. **Nguyễn Minh Tiến** - MSV: `B22DVCN308` (Trưởng nhóm - Người 1)
  2. **Đồng Duy Phúc** - MSV: `B22DVCN152` (Thành viên - Người 2)

---

## 1. Phân công công việc (Theo yêu cầu đề bài)

| Thành viên | Phân công nhiệm vụ | Chi tiết thực hiện |
| :--- | :--- | :--- |
| **Nguyễn Minh Tiến**<br>(Người 1) | Xây dựng class User & Cấu trúc đồ thị | - Lớp `User`: mô hình hóa người dùng (ID, tên, tuổi, chuyên ngành, quê quán, sở thích, danh sách bạn bè).<br>- Lớp `SocialGraph`: Cấu trúc đồ thị vô hướng dùng danh sách kề (`HashMap<String, Set<String>>` / `HashMap<String, List<String>>`).<br>- Thêm/xóa đỉnh, thêm/xóa cạnh bạn bè hai chiều, kiểm tra bạn bè.<br>- File I/O (`exportToFile`, `importFromFile`).<br>- Giao diện Menu tương tác người dùng (`SocialNetworkApp`, `Main`). |
| **Đồng Duy Phúc**<br>(Người 2) | Viết các hàm xử lý phân tích dữ liệu mạng xã hội | - Lớp `SocialAnalyzer`: Triển khai toàn bộ giải thuật phân tích đồ thị.<br>- **Chức năng 1**: Xem bạn bè chung giữa 2 người (Mutual Friends).<br>- **Chức năng 2**: Gợi ý bạn mới bằng **BFS** (Friends of Friends) kèm xếp hạng.<br>- **Chức năng 2**: Gợi ý bạn mới bằng **DFS** (Khám phá mạng lưới sâu).<br>- **Chức năng 3**: Tìm đường kết nối ngắn nhất giữa 2 người bằng **BFS** (Degrees of Separation) kèm truy vết lộ trình.<br>- Tính năng mở rộng: Thành phần liên thông (Connected Components), Top Influencers, Độ tương đồng Jaccard.<br>- Bộ kiểm thử tự động toàn diện (`SocialNetworkTest`). |

---

## 2. Cấu trúc thư mục dự án

```text
├── src/
│   └── ptit/dsa/socialnetwork/
│       ├── Main.java                          # Điểm khởi chạy chính của ứng dụng
│       ├── model/
│       │   └── User.java                      # Thực thể người dùng (Node)
│       ├── graph/
│       │   └── SocialGraph.java               # Đồ thị vô hướng mạng xã hội (Danh sách kề)
│       ├── analysis/
│       │   ├── SocialAnalyzer.java            # Các giải thuật BFS, DFS, Shortest Path, Mutual Friends
│       │   ├── FriendRecommendation.java      # Model kết quả gợi ý kết bạn
│       │   └── ConnectionPath.java            # Model kết quả đường kết nối ngắn nhất
│       ├── app/
│       │   └── SocialNetworkApp.java          # Menu CLI tương tác trực quan
│       └── test/
│           └── SocialNetworkTest.java         # Bộ kiểm thử tự động (100% Pass)
├── data/
│   └── social_data.txt                        # Dữ liệu sinh viên PTIT mẫu
├── run.bat                                    # Script chạy trên Windows (1-click)
├── run.sh                                     # Script chạy trên Linux/WSL
├── Phân tích mạng xã hội bằng đồ thị.docx    # Báo cáo Word hoàn chỉnh chuẩn PTIT
└── README.md                                  # Tài liệu hướng dẫn sử dụng
```

---

## 3. Hướng dẫn biên dịch và thực thi

### Cách 1: Trên Windows (Khuyên dùng)
- **Nếu dùng PowerShell** (trong VS Code hoặc Terminal):
  ```powershell
  ./run.ps1
  ```
  *(hoặc `powershell -ExecutionPolicy Bypass -File .\run.ps1`)*
- **Nếu dùng Command Prompt (CMD)** hoặc nhấp đúp:
  ```cmd
  run.bat
  ```
- Chọn:
  - Phím `1`: Khởi chạy ứng dụng tương tác dòng lệnh Menu (`SocialNetworkApp`).
  - Phím `2`: Chạy bộ kiểm thử tự động toàn diện (`SocialNetworkTest`).

### Cách 2: Bằng dòng lệnh terminal (Linux / WSL / Command Prompt)
```bash
# Biên dịch toàn bộ mã nguồn
javac -encoding UTF-8 -d bin src/ptit/dsa/socialnetwork/model/*.java \
      src/ptit/dsa/socialnetwork/graph/*.java \
      src/ptit/dsa/socialnetwork/analysis/*.java \
      src/ptit/dsa/socialnetwork/app/*.java \
      src/ptit/dsa/socialnetwork/*.java \
      src/ptit/dsa/socialnetwork/test/*.java

# Chạy ứng dụng Menu tương tác
java -cp bin ptit.dsa.socialnetwork.Main

# Hoặc chạy kiểm thử tự động
java -cp bin ptit.dsa.socialnetwork.test.SocialNetworkTest
```

---

## 4. Báo cáo bài tập lớn (.docx)
File báo cáo `Phân tích mạng xã hội bằng đồ thị.docx` đã được hoàn thiện đầy đủ:
- **Trang bìa**: Giữ nguyên mẫu bìa chuẩn khung viền hoa văn PTIT, thông tin nhóm 12, lớp D22VHCNPM02 và GVHD ThS. Vũ Đình Tân.
- **Lời cảm ơn & Lời cam đoan**: Trang trọng, đúng chuẩn học thuật.
- **Bảng phân công nhiệm vụ**: Chi tiết Người 1 và Người 2 theo yêu cầu đề bài.
- **Mục lục & Danh mục thuật ngữ**: Rõ ràng, dễ tra cứu.
- **Chương 1**: Tổng quan đề tài và phân tích yêu cầu.
- **Chương 2**: Cơ sở lý thuyết đồ thị vô hướng, danh sách kề `HashMap<String, Set<String>>`, phân tích độ phức tạp $O(V + E)$ của BFS, DFS, Shortest path, Jaccard.
- **Chương 3**: Thiết kế & cài đặt hệ thống (Kiến trúc phân lớp, thiết kế chi tiết từng class và giải thích mã nguồn).
- **Chương 4**: Kết quả thực nghiệm và đánh giá (ảnh chụp nhật ký console, dữ liệu sinh viên PTIT, bảng kết quả kiểm thử tự động 8/8 test cases đạt 100%).
- **Chương 5**: Kết luận và hướng phát triển tương lai.
- **Tài liệu tham khảo**: Các giáo trình chuẩn PTIT và tài liệu thuật toán uy tín.
