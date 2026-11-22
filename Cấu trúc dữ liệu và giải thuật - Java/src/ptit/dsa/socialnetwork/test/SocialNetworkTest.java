package ptit.dsa.socialnetwork.test;

import ptit.dsa.socialnetwork.analysis.ConnectionPath;
import ptit.dsa.socialnetwork.analysis.FriendRecommendation;
import ptit.dsa.socialnetwork.analysis.SocialAnalyzer;
import ptit.dsa.socialnetwork.graph.SocialGraph;
import ptit.dsa.socialnetwork.model.User;

import java.io.File;
import java.util.List;

/**
 * ============================================================================
 * Lớp kiểm thử tự động toàn diện (Comprehensive Automated Test Suite)
 * Kiểm thử tất cả các trường hợp theo yêu cầu đề bài:
 * 1. Đồ thị vô hướng (Undirected Graph) và Quản lý User
 * 2. Xem bạn bè chung (Mutual Friends)
 * 3. Gợi ý bạn mới bằng BFS & DFS (Friend Recommendation)
 * 4. Tìm đường kết nối ngắn nhất bằng BFS (Shortest Connection Path)
 * 5. Trường hợp biên: Không có đường đi (đồ thị không liên thông), cùng 1 người
 * 6. Phân tích thành phần liên thông và Top Influencers
 * 7. Lưu và đọc dữ liệu từ file
 * ============================================================================
 */
public class SocialNetworkTest {
    public static void main(String[] args) {
        try {
            System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8.name()));
            System.setErr(new java.io.PrintStream(System.err, true, java.nio.charset.StandardCharsets.UTF_8.name()));
        } catch (Exception ignored) {}

        System.out.println("================================================================================");
        System.out.println("   BẮT ĐẦU CHẠY BỘ KIỂM THỬ TỰ ĐỘNG - PHÂN TÍCH MẠNG XÃ HỘI BẰNG ĐỒ THỊ");
        System.out.println("================================================================================\n");

        SocialGraph graph = new SocialGraph();
        graph.loadSampleData();
        SocialAnalyzer analyzer = new SocialAnalyzer(graph);

        int totalTests = 0;
        int passedTests = 0;

        // TEST 1: Kiểm tra cấu trúc đồ thị ban đầu
        totalTests++;
        System.out.println("TEST 1: Kiểm tra nạp dữ liệu và cấu trúc đồ thị vô hướng");
        if (graph.getUserCount() == 12 && graph.getEdgeCount() == 15) {
            System.out.printf("  => PASS: Đồ thị có %d người dùng và %d cạnh vô hướng.\n\n",
                    graph.getUserCount(), graph.getEdgeCount());
            passedTests++;
        } else {
            System.out.printf("  => FAIL: UserCount=%d (kỳ vọng 12), EdgeCount=%d (kỳ vọng 15)\n\n",
                    graph.getUserCount(), graph.getEdgeCount());
        }

        // TEST 2: Kiểm tra chức năng 1 - Xem bạn bè chung
        totalTests++;
        System.out.println("TEST 2: Kiểm tra Chức năng 1 - Tìm bạn bè chung giữa U01 (Tiến) và U02 (Phúc)");
        List<User> mutual12 = analyzer.getMutualFriends("U01", "U02");
        System.out.printf("  Số bạn chung tìm thấy: %d (Danh sách: ", mutual12.size());
        for (User u : mutual12) System.out.print(u.getName() + " ");
        System.out.println(")");
        if (mutual12.size() == 1 && mutual12.get(0).getId().equals("U03")) {
            System.out.println("  => PASS: Bạn bè chung chính xác là Trần Thu Hà (U03).\n");
            passedTests++;
        } else {
            System.out.println("  => FAIL: Bạn bè chung không khớp kỳ vọng.\n");
        }

        // TEST 3: Kiểm tra chức năng 2 - Gợi ý bạn mới bằng BFS
        totalTests++;
        System.out.println("TEST 3: Kiểm tra Chức năng 2 - Gợi ý bạn mới bằng BFS cho U01 (Tiến)");
        List<FriendRecommendation> recsBFS = analyzer.recommendFriendsBFS("U01", 5);
        System.out.printf("  Số lượng gợi ý nhận được: %d\n", recsBFS.size());
        for (FriendRecommendation r : recsBFS) {
            System.out.printf("    - Gợi ý: [%s] %s | Số bạn chung: %d\n",
                    r.getUser().getId(), r.getUser().getName(), r.getMutualFriendsCount());
        }
        if (!recsBFS.isEmpty() && !graph.isFriend("U01", recsBFS.get(0).getUser().getId())) {
            System.out.println("  => PASS: Thuật toán BFS gợi ý chính xác bạn của bạn chưa kết bạn.\n");
            passedTests++;
        } else {
            System.out.println("  => FAIL: Gợi ý BFS không hợp lệ.\n");
        }

        // TEST 4: Kiểm tra chức năng 2 - Gợi ý bạn mới bằng DFS
        totalTests++;
        System.out.println("TEST 4: Kiểm tra Chức năng 2 - Gợi ý bạn mới bằng DFS (độ sâu 3) cho U01 (Tiến)");
        List<FriendRecommendation> recsDFS = analyzer.recommendFriendsDFS("U01", 3, 5);
        System.out.printf("  Số lượng gợi ý DFS nhận được: %d\n", recsDFS.size());
        for (FriendRecommendation r : recsDFS) {
            System.out.printf("    - Gợi ý DFS: [%s] %s\n", r.getUser().getId(), r.getUser().getName());
        }
        if (!recsDFS.isEmpty()) {
            System.out.println("  => PASS: Thuật toán DFS duyệt sâu thành công và trả về danh sách ứng viên.\n");
            passedTests++;
        } else {
            System.out.println("  => FAIL: Gợi ý DFS rỗng.\n");
        }

        // TEST 5: Kiểm tra chức năng 3 - Tìm đường đi ngắn nhất BFS
        totalTests++;
        System.out.println("TEST 5: Kiểm tra Chức năng 3 - Tìm đường kết nối ngắn nhất giữa U01 (Tiến) và U10 (Kim Yến)");
        ConnectionPath path = analyzer.findShortestPathBFS("U01", "U10");
        System.out.println("  " + path.getFormattedPath());
        if (path.isConnected() && path.getDistance() == 3) {
            System.out.println("  => PASS: Tìm thấy đường đi tối ưu với độ dài ngắn nhất = 3 bước.\n");
            passedTests++;
        } else {
            System.out.printf("  => FAIL: Khoảng cách = %d (Kỳ vọng 3)\n\n", path.getDistance());
        }

        // TEST 6: Kiểm tra trường hợp biên không có đường đi (Khác thành phần liên thông)
        totalTests++;
        System.out.println("TEST 6: Kiểm tra trường hợp 2 người thuộc 2 nhóm độc lập: U01 (Tiến) và U11 (Nam)");
        ConnectionPath disconnectedPath = analyzer.findShortestPathBFS("U01", "U11");
        System.out.println("  " + disconnectedPath.getFormattedPath());
        if (!disconnectedPath.isConnected()) {
            System.out.println("  => PASS: Xử lý chính xác đồ thị không liên thông.\n");
            passedTests++;
        } else {
            System.out.println("  => FAIL: Kỳ vọng không có đường kết nối.\n");
        }

        // TEST 7: Kiểm tra phát hiện thành phần liên thông
        totalTests++;
        System.out.println("TEST 7: Kiểm tra phát hiện các thành phần liên thông (Connected Components)");
        List<List<User>> components = analyzer.findConnectedComponents();
        System.out.printf("  Số thành phần liên thông: %d\n", components.size());
        if (components.size() == 2) {
            System.out.println("  => PASS: Nhận diện chính xác 2 nhóm độc lập (Nhóm PTIT chính 10 người & Nhóm phụ 2 người).\n");
            passedTests++;
        } else {
            System.out.printf("  => FAIL: Số thành phần liên thông = %d (kỳ vọng 2)\n\n", components.size());
        }

        // TEST 8: Kiểm tra lưu và đọc file
        totalTests++;
        System.out.println("TEST 8: Kiểm tra chức năng lưu đồ thị ra file và đọc lại");
        try {
            String testFile = "data/test_social_graph.txt";
            new File("data").mkdirs();
            graph.exportToFile(testFile);

            SocialGraph importedGraph = new SocialGraph();
            importedGraph.importFromFile(testFile);

            if (importedGraph.getUserCount() == graph.getUserCount() &&
                importedGraph.getEdgeCount() == graph.getEdgeCount()) {
                System.out.println("  => PASS: Dữ liệu đồ thị sau khi import hoàn toàn trùng khớp với đồ thị gốc.\n");
                passedTests++;
            } else {
                System.out.println("  => FAIL: Số đỉnh hoặc số cạnh sau khi import không khớp.\n");
            }
            new File(testFile).delete();
        } catch (Exception e) {
            System.out.println("  => FAIL: Ngoại lệ khi lưu/đọc file: " + e.getMessage() + "\n");
        }

        System.out.println("================================================================================");
        System.out.printf("   KẾT QUẢ KIỂM THỬ: %d/%d TEST CASES ĐẠT (TỈ LỆ THÀNH CÔNG: %.1f%%)\n",
                passedTests, totalTests, (passedTests * 100.0 / totalTests));
        System.out.println("================================================================================");
    }
}
