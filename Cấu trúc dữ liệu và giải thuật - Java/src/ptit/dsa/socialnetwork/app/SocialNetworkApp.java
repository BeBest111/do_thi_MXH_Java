package ptit.dsa.socialnetwork.app;

import ptit.dsa.socialnetwork.analysis.ConnectionPath;
import ptit.dsa.socialnetwork.analysis.FriendRecommendation;
import ptit.dsa.socialnetwork.analysis.SocialAnalyzer;
import ptit.dsa.socialnetwork.graph.SocialGraph;
import ptit.dsa.socialnetwork.model.User;

import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * ============================================================================
 * HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG - KHOA CÔNG NGHỆ THÔNG TIN
 * Môn học: Cấu trúc dữ liệu và giải thuật - Java
 * Đề tài: Phân tích mạng xã hội bằng đồ thị (Graph Traversal)
 * ============================================================================
 * Chương trình điều khiển giao diện Console tương tác Menu trực quan.
 */
public class SocialNetworkApp {
    private final SocialGraph graph;
    private final SocialAnalyzer analyzer;
    private final Scanner scanner;

    public SocialNetworkApp() {
        this.graph = new SocialGraph();
        this.graph.loadSampleData(); // Nạp sẵn dữ liệu sinh viên PTIT phong phú
        this.analyzer = new SocialAnalyzer(this.graph);
        this.scanner = new Scanner(System.in, java.nio.charset.StandardCharsets.UTF_8.name());
    }

    public void run() {
        printBanner();
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print(">> Nhập lựa chọn của bạn (0-12): ");
            String choice = scanner.nextLine().trim();

            System.out.println();
            switch (choice) {
                case "1":
                    handleDisplayGraph();
                    break;
                case "2":
                    handleAddUser();
                    break;
                case "3":
                    handleViewUserDetails();
                    break;
                case "4":
                    handleAddFriendship();
                    break;
                case "5":
                    handleRemoveFriendship();
                    break;
                case "6":
                    handleViewFriends();
                    break;
                case "7":
                    handleMutualFriends();
                    break;
                case "8":
                    handleRecommendFriendsBFS();
                    break;
                case "9":
                    handleRecommendFriendsDFS();
                    break;
                case "10":
                    handleShortestPathBFS();
                    break;
                case "11":
                    handleAdvancedAnalysis();
                    break;
                case "12":
                    handleFileIO();
                    break;
                case "0":
                    System.out.println("Cảm ơn bạn đã sử dụng chương trình! Hẹn gặp lại.");
                    running = false;
                    break;
                default:
                    System.out.println("[X] Lựa chọn không hợp lệ! Vui lòng chọn từ 0 đến 12.");
            }
            if (running) {
                System.out.print("\nNhấn phím Enter để tiếp tục...");
                scanner.nextLine();
            }
        }
    }

    private void printBanner() {
        System.out.println("+==============================================================================+");
        System.out.println("|                 HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG                      |");
        System.out.println("|                       KHOA CÔNG NGHỆ THÔNG TIN                               |");
        System.out.println("|            BÀI TẬP LỚN: PHÂN TÍCH MẠNG XÃ HỘI BẰNG ĐỒ THỊ (JAVA)             |");
        System.out.println("|  GVHD: ThS. Vũ Đình Tân                                                      |");
        System.out.println("|  Sinh viên: Nguyễn Minh Tiến (B22DVCN308) - Đồng Duy Phúc (B22DVCN152)       |");
        System.out.println("+==============================================================================+");
    }

    private void printMenu() {
        System.out.println("\n============================= MENU CHỨC NĂNG =============================");
        System.out.println("  1.  Hiển thị toàn bộ mạng xã hội (Danh sách kề)");
        System.out.println("  2.  Thêm người dùng mới vào hệ thống");
        System.out.println("  3.  Tra cứu thông tin chi tiết một người dùng");
        System.out.println("  4.  Thiết lập quan hệ kết bạn (Thêm cạnh đồ thị)");
        System.out.println("  5.  Hủy quan hệ kết bạn (Xóa cạnh đồ thị)");
        System.out.println("  6.  Xem danh sách bạn bè của một người");
        System.out.println("  7.  [CHỨC NĂNG CHÍNH] Tìm bạn bè chung giữa 2 người (Mutual Friends)");
        System.out.println("  8.  [CHỨC NĂNG CHÍNH] Gợi ý bạn mới bằng BFS (Friends of Friends)");
        System.out.println("  9.  [CHỨC NĂNG CHÍNH] Gợi ý bạn mới bằng DFS (Duyệt theo chiều sâu)");
        System.out.println("  10. [CHỨC NĂNG CHÍNH] Tìm đường kết nối ngắn nhất bằng BFS (Shortest Path)");
        System.out.println("  11. [MỞ RỘNG] Thống kê mạng: Top ảnh hưởng, Nhóm độc lập, Jaccard");
        System.out.println("  12. Lưu / Nạp dữ liệu từ file (txt/csv) hoặc hoàn tác dữ liệu mẫu");
        System.out.println("  0.  Thoát chương trình");
        System.out.println("==========================================================================");
    }

    private void handleDisplayGraph() {
        graph.displayGraph();
    }

    private void handleAddUser() {
        System.out.println("--- THÊM NGƯỜI DÙNG MỚI ---");
        System.out.print("Nhập ID người dùng (ví dụ U13, B22DVCN...): ");
        String id = scanner.nextLine().trim();
        if (graph.containsUser(id)) {
            System.out.println("❌ Lỗi: ID người dùng đã tồn tại trên mạng xã hội!");
            return;
        }
        System.out.print("Nhập họ và tên: ");
        String name = scanner.nextLine().trim();
        System.out.print("Nhập tuổi: ");
        int age = 20;
        try {
            age = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException ignored) {}

        System.out.print("Nhập giới tính (Nam/Nữ): ");
        String gender = scanner.nextLine().trim();
        System.out.print("Nhập ngành học (CNTT/ATTT/DTVT/v.v.): ");
        String major = scanner.nextLine().trim();
        System.out.print("Nhập quê quán: ");
        String hometown = scanner.nextLine().trim();
        System.out.print("Nhập các sở thích (cách nhau bởi dấu phẩy): ");
        String hobbiesStr = scanner.nextLine().trim();

        User user = new User(id, name, age, gender, major, hometown);
        if (!hobbiesStr.isEmpty()) {
            for (String h : hobbiesStr.split(",")) {
                user.addHobby(h.trim());
            }
        }

        if (graph.addUser(user)) {
            System.out.printf("✅ Đã thêm người dùng '%s' [%s] thành công vào đồ thị!\n", name, id);
        } else {
            System.out.println("❌ Thêm người dùng thất bại.");
        }
    }

    private void handleViewUserDetails() {
        System.out.print("Nhập ID người dùng cần xem: ");
        String id = scanner.nextLine().trim();
        User u = graph.getUser(id);
        if (u == null) {
            System.out.println("❌ Không tìm thấy người dùng có ID: " + id);
            return;
        }
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(u.toDetailString());
        System.out.println("--------------------------------------------------------------------------------");
    }

    private void handleAddFriendship() {
        System.out.println("--- THIẾT LẬP KẾT BẠN (CẠNH VÔ HƯỚNG) ---");
        System.out.print("Nhập ID người thứ nhất: ");
        String u1 = scanner.nextLine().trim();
        System.out.print("Nhập ID người thứ hai: ");
        String u2 = scanner.nextLine().trim();

        if (graph.addFriendship(u1, u2)) {
            System.out.printf("✅ Đã thiết lập quan hệ kết bạn thành công giữa [%s - %s] và [%s - %s]!\n",
                    u1, graph.getUser(u1).getName(), u2, graph.getUser(u2).getName());
        } else {
            System.out.println("❌ Kết bạn thất bại! Có thể do ID không tồn tại, người dùng trùng nhau, hoặc đã là bạn bè.");
        }
    }

    private void handleRemoveFriendship() {
        System.out.println("--- HỦY QUAN HỆ KẾT BẠN ---");
        System.out.print("Nhập ID người thứ nhất: ");
        String u1 = scanner.nextLine().trim();
        System.out.print("Nhập ID người thứ hai: ");
        String u2 = scanner.nextLine().trim();

        if (graph.removeFriendship(u1, u2)) {
            System.out.printf("✅ Đã hủy quan hệ kết bạn giữa [%s] và [%s]!\n", u1, u2);
        } else {
            System.out.println("❌ Không thể hủy quan hệ kết bạn (Họ chưa từng kết bạn hoặc ID không hợp lệ).");
        }
    }

    private void handleViewFriends() {
        System.out.print("Nhập ID người dùng: ");
        String id = scanner.nextLine().trim();
        User user = graph.getUser(id);
        if (user == null) {
            System.out.println("❌ Không tìm thấy người dùng có ID: " + id);
            return;
        }
        List<User> friends = graph.getFriends(id);
        System.out.printf("\nDanh sách bạn bè của %s [%s] (Tổng cộng: %d người):\n",
                user.getName(), id, friends.size());
        if (friends.isEmpty()) {
            System.out.println("   (Chưa có bạn bè)");
        } else {
            for (int i = 0; i < friends.size(); i++) {
                User f = friends.get(i);
                System.out.printf("   %2d. [%s] %-20s - Ngành: %-10s - Quê: %s\n",
                        i + 1, f.getId(), f.getName(), f.getMajor(), f.getHometown());
            }
        }
    }

    private void handleMutualFriends() {
        System.out.println("--- [CHỨC NĂNG 1] TÌM BẠN BÈ CHUNG GIỮA 2 NGƯỜI ---");
        System.out.print("Nhập ID người thứ nhất: ");
        String u1 = scanner.nextLine().trim();
        System.out.print("Nhập ID người thứ hai: ");
        String u2 = scanner.nextLine().trim();

        User user1 = graph.getUser(u1);
        User user2 = graph.getUser(u2);
        if (user1 == null || user2 == null) {
            System.out.println("❌ Một trong hai người dùng không tồn tại trên hệ thống!");
            return;
        }

        List<User> mutualList = analyzer.getMutualFriends(u1, u2);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("🤝 BẠN BÈ CHUNG GIỮA: %s [%s] VÀ %s [%s]\n",
                user1.getName(), u1, user2.getName(), u2);
        System.out.printf("Số lượng bạn chung: %d người\n", mutualList.size());
        if (mutualList.isEmpty()) {
            System.out.println("=> Hai người dùng này hiện chưa có người bạn chung nào.");
        } else {
            for (int i = 0; i < mutualList.size(); i++) {
                User f = mutualList.get(i);
                System.out.printf("   %2d. [%s] %-22s | Ngành: %-12s | Tuổi: %d\n",
                        i + 1, f.getId(), f.getName(), f.getMajor(), f.getAge());
            }
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    private void handleRecommendFriendsBFS() {
        System.out.println("--- [CHỨC NĂNG 2] GỢI Ý BẠN MỚI BẰNG THUẬT TOÁN BFS (FRIENDS OF FRIENDS) ---");
        System.out.print("Nhập ID người dùng cần nhận gợi ý kết bạn: ");
        String id = scanner.nextLine().trim();
        User user = graph.getUser(id);
        if (user == null) {
            System.out.println("❌ Không tìm thấy người dùng có ID: " + id);
            return;
        }

        List<FriendRecommendation> recs = analyzer.recommendFriendsBFS(id, 5);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("💡 KẾT QUẢ GỢI Ý KẾT BẠN (BFS) CHO: %s [%s]\n", user.getName(), id);
        if (recs.isEmpty()) {
            System.out.println("Hiện không có gợi ý kết bạn nào phù hợp qua mạng lưới bạn bè.");
        } else {
            for (int i = 0; i < recs.size(); i++) {
                System.out.printf("%d. %s", i + 1, recs.get(i).toString());
            }
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    private void handleRecommendFriendsDFS() {
        System.out.println("--- [CHỨC NĂNG 2] GỢI Ý BẠN MỚI BẰNG THUẬT TOÁN DFS (DUYỆT THEO CHIỀU SÂU) ---");
        System.out.print("Nhập ID người dùng cần nhận gợi ý kết bạn: ");
        String id = scanner.nextLine().trim();
        User user = graph.getUser(id);
        if (user == null) {
            System.out.println("❌ Không tìm thấy người dùng có ID: " + id);
            return;
        }

        List<FriendRecommendation> recs = analyzer.recommendFriendsDFS(id, 3, 5);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("💡 KẾT QUẢ GỢI Ý KẾT BẠN (DFS - Độ sâu 3) CHO: %s [%s]\n", user.getName(), id);
        if (recs.isEmpty()) {
            System.out.println("Hiện không có gợi ý kết bạn nào từ các nhánh sâu.");
        } else {
            for (int i = 0; i < recs.size(); i++) {
                System.out.printf("%d. %s", i + 1, recs.get(i).toString());
            }
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    private void handleShortestPathBFS() {
        System.out.println("--- [CHỨC NĂNG 3] TÌM ĐƯỜNG KẾT NỐI NGẮN NHẤT BẰNG BFS (DEGREES OF SEPARATION) ---");
        System.out.print("Nhập ID người bắt đầu (Source): ");
        String u1 = scanner.nextLine().trim();
        System.out.print("Nhập ID người đích đến (Target): ");
        String u2 = scanner.nextLine().trim();

        if (!graph.containsUser(u1) || !graph.containsUser(u2)) {
            System.out.println("❌ Một trong hai người dùng không tồn tại!");
            return;
        }

        ConnectionPath path = analyzer.findShortestPathBFS(u1, u2);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("🔍 KẾT QUẢ TÌM ĐƯỜNG KẾT NỐI:");
        System.out.println(path.getFormattedPath());
        System.out.println("--------------------------------------------------------------------------------");
    }

    private void handleAdvancedAnalysis() {
        System.out.println("--- [TÍNH NĂNG MỞ RỘNG] PHÂN TÍCH CHUYÊN SÂU MẠNG XÃ HỘI ---");
        System.out.println("a. Xem Top người có tầm ảnh hưởng (Degree Centrality)");
        System.out.println("b. Phân tích các thành phần liên thông (Nhóm độc lập)");
        System.out.println("c. Tính độ tương đồng bạn bè Jaccard Similarity giữa 2 người");
        System.out.print("Chọn chức năng con (a/b/c): ");
        String sub = scanner.nextLine().trim().toLowerCase();

        switch (sub) {
            case "a":
                List<User> top = analyzer.getTopInfluencers(5);
                System.out.println("\n⭐ TOP 5 NGƯỜI DÙNG CÓ NHIỀU BẠN BÈ NHẤT MẠNG XÃ HỘI:");
                for (int i = 0; i < top.size(); i++) {
                    User u = top.get(i);
                    System.out.printf("  Top %d: [%s] %-20s - Số bạn bè: %d (%s)\n",
                            i + 1, u.getId(), u.getName(), u.getFriendCount(), u.getMajor());
                }
                break;
            case "b":
                List<List<User>> comps = analyzer.findConnectedComponents();
                System.out.printf("\n🌐 MẠNG XÃ HỘI CÓ TỔNG CỘNG: %d THÀNH PHẦN LIÊN THÔNG (NHÓM ĐỘC LẬP):\n", comps.size());
                for (int i = 0; i < comps.size(); i++) {
                    List<User> comp = comps.get(i);
                    System.out.printf("  Nhóm %d (%d thành viên): ", i + 1, comp.size());
                    for (int j = 0; j < comp.size(); j++) {
                        System.out.print(comp.get(j).getName());
                        if (j < comp.size() - 1) System.out.print(", ");
                    }
                    System.out.println();
                }
                break;
            case "c":
                System.out.print("Nhập ID người 1: ");
                String u1 = scanner.nextLine().trim();
                System.out.print("Nhập ID người 2: ");
                String u2 = scanner.nextLine().trim();
                if (graph.containsUser(u1) && graph.containsUser(u2)) {
                    double jaccard = analyzer.calculateJaccardSimilarity(u1, u2);
                    System.out.printf("Hệ số tương đồng Jaccard giữa [%s] và [%s]: %.4f (%.2f%%)\n",
                            u1, u2, jaccard, jaccard * 100.0);
                } else {
                    System.out.println("❌ ID không tồn tại.");
                }
                break;
            default:
                System.out.println("Lựa chọn không hợp lệ.");
        }
    }

    private void handleFileIO() {
        System.out.println("--- QUẢN LÝ DỮ LIỆU FILE ---");
        System.out.println("1. Xuất dữ liệu đồ thị hiện tại ra file data/social_data.txt");
        System.out.println("2. Đọc dữ liệu đồ thị từ file data/social_data.txt");
        System.out.println("3. Khôi phục lại dữ liệu mẫu gốc PTIT");
        System.out.print("Chọn thao tác (1/2/3): ");
        String sub = scanner.nextLine().trim();

        String defaultPath = "data/social_data.txt";
        try {
            if ("1".equals(sub)) {
                new File("data").mkdirs();
                graph.exportToFile(defaultPath);
                System.out.println("✅ Đã xuất dữ liệu thành công ra file: " + defaultPath);
            } else if ("2".equals(sub)) {
                graph.importFromFile(defaultPath);
                System.out.println("✅ Đã nạp dữ liệu thành công từ file: " + defaultPath);
            } else if ("3".equals(sub)) {
                graph.loadSampleData();
                System.out.println("✅ Đã khôi phục thành công bộ dữ liệu mẫu sinh viên PTIT!");
            } else {
                System.out.println("Lựa chọn không hợp lệ.");
            }
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi xử lý file: " + e.getMessage());
        }
    }
}
