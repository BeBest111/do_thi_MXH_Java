package ptit.dsa.socialnetwork.analysis;

import ptit.dsa.socialnetwork.graph.SocialGraph;
import ptit.dsa.socialnetwork.model.User;

import java.util.*;

/**
 * ============================================================================
 * HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG - KHOA CÔNG NGHỆ THÔNG TIN
 * Môn học: Cấu trúc dữ liệu và giải thuật - Java
 * Đề tài: Phân tích mạng xã hội bằng đồ thị (Graph Traversal)
 * Phân công: Người 2 - Đồng Duy Phúc (B22DVCN152)
 * ============================================================================
 * Lớp cài đặt các giải thuật phân tích dữ liệu mạng xã hội:
 * 1. Xem bạn bè chung giữa 2 người (Mutual Friends).
 * 2. Gợi ý bạn mới bằng giải thuật duyệt theo chiều rộng (BFS Friend Recommendation).
 * 3. Gợi ý bạn mới bằng giải thuật duyệt theo chiều sâu (DFS Friend Recommendation).
 * 4. Tìm đường kết nối ngắn nhất giữa 2 người (BFS Shortest Path - Degrees of Separation).
 * 5. Phân tích các thành phần liên thông (Connected Components).
 * 6. Tìm Top người dùng có tầm ảnh hưởng lớn nhất (Degree Centrality).
 * 7. Tính hệ số tương đồng Jaccard giữa hai người dùng.
 */
public class SocialAnalyzer {
    private final SocialGraph graph;

    public SocialAnalyzer(SocialGraph graph) {
        this.graph = Objects.requireNonNull(graph, "Đồ thị mạng xã hội không được null");
    }

    // ========================================================================
    // CHỨC NĂNG 1: XEM BẠN BÈ CHUNG (MUTUAL FRIENDS)
    // ========================================================================

    /**
     * Tìm tập hợp các bạn bè chung giữa hai người dùng.
     * @param userId1 ID người dùng thứ nhất
     * @param userId2 ID người dùng thứ hai
     * @return Danh sách các User là bạn bè của cả 2 người
     */
    public List<User> getMutualFriends(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return Collections.emptyList();
        userId1 = userId1.trim();
        userId2 = userId2.trim();

        if (!graph.containsUser(userId1) || !graph.containsUser(userId2)) {
            return Collections.emptyList();
        }

        Set<String> friends1 = graph.getFriendIds(userId1);
        Set<String> friends2 = graph.getFriendIds(userId2);

        // Tối ưu: duyệt tập nhỏ hơn và tra cứu O(1) trong tập lớn hơn -> O(min(d1, d2))
        Set<String> small = friends1.size() <= friends2.size() ? friends1 : friends2;
        Set<String> large = small == friends1 ? friends2 : friends1;
        Set<String> commonIds = new LinkedHashSet<>();
        for (String id : small) {
            if (large.contains(id)) commonIds.add(id);
        }

        List<User> mutualUsers = new ArrayList<>();
        for (String id : commonIds) {
            User u = graph.getUser(id);
            if (u != null) {
                mutualUsers.add(u);
            }
        }
        return mutualUsers;
    }

    /**
     * Lấy số lượng bạn bè chung giữa hai người dùng.
     */
    public int getMutualFriendCount(String userId1, String userId2) {
        return getMutualFriends(userId1, userId2).size();
    }

    // ========================================================================
    // CHỨC NĂNG 2: GỢI Ý BẠN MỚI BẰNG DUYỆT THEO CHIỀU RỘNG (BFS)
    // ========================================================================

    /**
     * Gợi ý bạn mới bằng thuật toán BFS dựa trên mô hình "Bạn của bạn" (Friends of Friends).
     * BFS duyệt theo từng tầng (Level/Layer):
     * - Tầng 0: Người dùng gốc
     * - Tầng 1: Bạn bè trực tiếp (loại trừ)
     * - Tầng 2: Bạn của bạn (nhóm ứng viên tiềm năng cao nhất)
     * Sau đó xếp hạng các ứng viên theo:
     * 1. Số bạn bè chung (giảm dần)
     * 2. Điểm tương đồng sở thích & ngành học
     *
     * @param userId ID người dùng cần nhận gợi ý
     * @param maxResults Số lượng gợi ý tối đa muốn lấy
     * @return Danh sách gợi ý đã được sắp xếp từ phù hợp nhất đến ít hơn
     */
    public List<FriendRecommendation> recommendFriendsBFS(String userId, int maxResults) {
        if (userId == null || !graph.containsUser(userId)) {
            return Collections.emptyList();
        }
        userId = userId.trim();
        User rootUser = graph.getUser(userId);

        Set<String> directFriends = graph.getFriendIds(userId);
        Map<String, Integer> distanceMap = new HashMap<>();
        Queue<String> queue = new ArrayDeque<>();

        // Khởi tạo BFS
        queue.add(userId);
        distanceMap.put(userId, 0);

        // Ứng viên tầng 2 -> danh sách bạn chung (thu thập ngay trong lúc BFS,
        // tránh phải giao hai tập bạn bè lại cho từng ứng viên)
        Map<String, List<User>> candidates = new LinkedHashMap<>();

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            int currentDist = distanceMap.get(currentId);

            // Giới hạn duyệt đến tầng 2 để tập trung vào Friends of Friends
            if (currentDist >= 2) {
                continue;
            }

            for (String neighborId : graph.getFriendIds(currentId)) {
                if (!distanceMap.containsKey(neighborId)) {
                    distanceMap.put(neighborId, currentDist + 1);
                    queue.add(neighborId);
                }
                // Cạnh đi từ tầng 1 sang một nút tầng 2: currentId chính là một BẠN CHUNG
                if (currentDist == 1 && distanceMap.get(neighborId) == 2) {
                    candidates.computeIfAbsent(neighborId, k -> new ArrayList<>())
                              .add(graph.getUser(currentId));
                }
            }
        }

        // Chấm điểm và tạo danh sách gợi ý
        List<FriendRecommendation> recommendations = new ArrayList<>(candidates.size());
        for (Map.Entry<String, List<User>> e : candidates.entrySet()) {
            User candUser = graph.getUser(e.getKey());
            if (candUser == null) continue;
            List<User> mutualList = e.getValue();
            double totalScore = mutualList.size() * 2.0 + calculateAffinityScore(rootUser, candUser);
            recommendations.add(new FriendRecommendation(
                    candUser, mutualList.size(), mutualList, totalScore, "BFS (Friends of Friends)", 2));
        }

        // Sắp xếp giảm dần theo độ ưu tiên
        Collections.sort(recommendations);

        if (maxResults > 0 && recommendations.size() > maxResults) {
            return new ArrayList<>(recommendations.subList(0, maxResults));
        }
        return recommendations;
    }

    // ========================================================================
    // CHỨC NĂNG 3: GỢI Ý BẠN MỚI BẰNG DUYỆT THEO CHIỀU SÂU (DFS)
    // ========================================================================

    /**
     * Gợi ý bạn mới bằng thuật toán DFS (Khám phá sâu mạng lưới bạn bè).
     * DFS đi sâu theo từng nhánh kết nối để tìm những người ở khoảng cách xa hơn
     * hoặc ở các cộng đồng nhánh sâu có thể quen biết gián tiếp.
     *
     * @param userId ID người dùng cần nhận gợi ý
     * @param maxDepth Độ sâu tối đa khi duyệt DFS (thường từ 2 đến 3)
     * @param maxResults Số lượng gợi ý tối đa
     */
    public List<FriendRecommendation> recommendFriendsDFS(String userId, int maxDepth, int maxResults) {
        if (userId == null || !graph.containsUser(userId)) {
            return Collections.emptyList();
        }
        userId = userId.trim();
        User rootUser = graph.getUser(userId);
        Set<String> directFriends = graph.getFriendIds(userId);

        // bestDepth: độ sâu NHỎ NHẤT đã tìm thấy cho mỗi nút (cho phép duyệt lại nút khi tìm
        // được đường nông hơn, đảm bảo kết quả đúng với giới hạn maxDepth bất kể thứ tự duyệt)
        Map<String, Integer> bestDepth = new LinkedHashMap<>();

        dfsExplore(userId, 0, maxDepth, bestDepth);

        List<FriendRecommendation> recommendations = new ArrayList<>();
        for (Map.Entry<String, Integer> e : bestDepth.entrySet()) {
            String candId = e.getKey();
            if (candId.equals(userId) || directFriends.contains(candId)) continue;
            User candUser = graph.getUser(candId);
            if (candUser == null) continue;

            List<User> mutualList = getMutualFriends(userId, candId);
            int mutualCount = mutualList.size();
            double totalScore = mutualCount * 2.0 + calculateAffinityScore(rootUser, candUser);

            recommendations.add(new FriendRecommendation(
                    candUser, mutualCount, mutualList, totalScore,
                    "DFS (Deep Network Exploration)", e.getValue()));
        }

        Collections.sort(recommendations);

        if (maxResults > 0 && recommendations.size() > maxResults) {
            return new ArrayList<>(recommendations.subList(0, maxResults));
        }
        return recommendations;
    }

    private void dfsExplore(String currentId, int currentDepth, int maxDepth, Map<String, Integer> bestDepth) {
        Integer known = bestDepth.get(currentId);
        if (known != null && known <= currentDepth) {
            return; // Đã đến nút này bằng đường ngắn hơn hoặc bằng
        }
        bestDepth.put(currentId, currentDepth);

        if (currentDepth >= maxDepth) {
            return;
        }
        for (String neighborId : graph.getFriendIds(currentId)) {
            dfsExplore(neighborId, currentDepth + 1, maxDepth, bestDepth);
        }
    }

    // ========================================================================
    // CHỨC NĂNG 4: TÌM ĐƯỜNG KẾT NỐI NGẮN NHẤT GIỮA 2 NGƯỜI (BFS SHORTEST PATH)
    // ========================================================================

    /**
     * Tìm chuỗi liên kết bạn bè ngắn nhất giữa 2 người dùng bất kỳ trong mạng xã hội.
     * Áp dụng thuật toán Breadth-First Search (BFS) đảm bảo tính tối ưu (ít bước chuyển tiếp nhất)
     * trên đồ thị vô hướng không trọng số.
     *
     * @param sourceId ID người bắt đầu
     * @param targetId ID người đích
     * @return Đối tượng ConnectionPath chứa lộ trình chi tiết và khoảng cách kết nối
     */
    public ConnectionPath findShortestPathBFS(String sourceId, String targetId) {
        if (sourceId == null || targetId == null) {
            return ConnectionPath.notConnected(sourceId, targetId);
        }
        sourceId = sourceId.trim();
        targetId = targetId.trim();

        if (!graph.containsUser(sourceId) || !graph.containsUser(targetId)) {
            return ConnectionPath.notConnected(sourceId, targetId);
        }

        if (sourceId.equals(targetId)) {
            return ConnectionPath.sameUser(graph.getUser(sourceId));
        }

        // Hàng đợi BFS và mảng truy vết (parent)
        Queue<String> queue = new ArrayDeque<>();
        Map<String, String> parentMap = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(sourceId);
        visited.add(sourceId);
        parentMap.put(sourceId, null);

        boolean found = false;

        while (!queue.isEmpty()) {
            String current = queue.poll();

            for (String neighbor : graph.getFriendIds(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);

                    if (neighbor.equals(targetId)) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }

        if (!found) {
            return ConnectionPath.notConnected(sourceId, targetId);
        }

        // Truy vết ngược đường đi từ target về source
        LinkedList<User> path = new LinkedList<>();
        String curr = targetId;
        while (curr != null) {
            path.addFirst(graph.getUser(curr));
            curr = parentMap.get(curr);
        }

        int distance = path.size() - 1; // Số cạnh kết nối
        return new ConnectionPath(sourceId, targetId, true, distance, path);
    }

    // ========================================================================
    // TÍNH NĂNG MỞ RỘNG: THÀNH PHẦN LIÊN THÔNG, TẦM ẢNH HƯỞNG & JACCARD
    // ========================================================================

    /**
     * Phân tích các thành phần liên thông trong mạng xã hội.
     * Giúp nhận diện các mạng lưới/cộng đồng tách biệt nhau.
     * @return Danh sách các thành phần liên thông (mỗi thành phần là một danh sách User)
     */
    public List<List<User>> findConnectedComponents() {
        Set<String> visited = new HashSet<>();
        List<List<User>> components = new ArrayList<>();

        for (String userId : graph.getAllUserIds()) {
            if (!visited.contains(userId)) {
                List<User> comp = new ArrayList<>();
                Queue<String> queue = new ArrayDeque<>();

                queue.add(userId);
                visited.add(userId);

                while (!queue.isEmpty()) {
                    String curr = queue.poll();
                    comp.add(graph.getUser(curr));

                    for (String neighbor : graph.getFriendIds(curr)) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
                components.add(comp);
            }
        }
        return components;
    }

    /**
     * Tìm Top K người dùng có tầm ảnh hưởng lớn nhất dựa trên Degree Centrality
     * (Người có số lượng bạn bè trực tiếp nhiều nhất trong mạng).
     */
    public List<User> getTopInfluencers(int topK) {
        List<User> users = new ArrayList<>(graph.getAllUsers());
        users.sort((a, b) -> Integer.compare(b.getFriendCount(), a.getFriendCount()));
        if (topK > 0 && users.size() > topK) {
            return users.subList(0, topK);
        }
        return users;
    }

    /**
     * Tính hệ số tương đồng Jaccard giữa bạn bè của 2 người dùng:
     * J(A, B) = |Friends(A) ∩ Friends(B)| / |Friends(A) ∪ Friends(B)|
     */
    public double calculateJaccardSimilarity(String userId1, String userId2) {
        if (!graph.containsUser(userId1) || !graph.containsUser(userId2)) return 0.0;
        Set<String> f1 = graph.getFriendIds(userId1);
        Set<String> f2 = graph.getFriendIds(userId2);

        if (f1.isEmpty() && f2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(f1);
        intersection.retainAll(f2);

        Set<String> union = new HashSet<>(f1);
        union.addAll(f2);

        return (double) intersection.size() / union.size();
    }

    /**
     * Tính điểm tương thích sở thích và khoa/ngành giữa hai người dùng.
     */
    private double calculateAffinityScore(User u1, User u2) {
        double score = 0.0;
        if (u1.getMajor() != null && u1.getMajor().equalsIgnoreCase(u2.getMajor())) {
            score += 0.5; // Cùng ngành học
        }
        if (u1.getHometown() != null && u1.getHometown().equalsIgnoreCase(u2.getHometown())) {
            score += 0.5; // Cùng quê quán
        }
        // Sở thích chung (đếm trực tiếp, không cần copy tập)
        int common = 0;
        for (String h : u1.getHobbies()) {
            if (u2.getHobbies().contains(h)) common++;
        }
        score += common * 0.3;
        return score;
    }
}
