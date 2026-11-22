package ptit.dsa.socialnetwork;

import ptit.dsa.socialnetwork.app.SocialNetworkApp;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * ============================================================================
 * HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG - KHOA CÔNG NGHỆ THÔNG TIN
 * Môn học: Cấu trúc dữ liệu và giải thuật - Java
 * Đề tài: Phân tích mạng xã hội bằng đồ thị (Graph Traversal)
 * Giảng viên hướng dẫn: ThS. Vũ Đình Tân
 * Nhóm sinh viên thực hiện: Nhóm 12 - Lớp D22VHCNPM02
 * - Nguyễn Minh Tiến (B22DVCN308) - Trưởng nhóm (Người 1)
 * - Đồng Duy Phúc (B22DVCN152) - Thành viên (Người 2)
 * ============================================================================
 */
public class Main {
    public static void main(String[] args) {
        // Cấu hình UTF-8 toàn diện cho console xuất nhập trên Windows
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8.name()));
        } catch (Exception ignored) {}

        SocialNetworkApp app = new SocialNetworkApp();
        app.run();
    }
}
