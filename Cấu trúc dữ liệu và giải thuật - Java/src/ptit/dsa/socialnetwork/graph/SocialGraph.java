package ptit.dsa.socialnetwork.graph;

import ptit.dsa.socialnetwork.model.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * ============================================================================
 * HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG - KHOA CÔNG NGHỆ THÔNG TIN
 * Môn học: Cấu trúc dữ liệu và giải thuật - Java
 * Đề tài: Phân tích mạng xã hội bằng đồ thị (Graph Traversal)
 * Phân công: Người 1 - Nguyễn Minh Tiến (B22DVCN308)
 * ============================================================================
 * Lớp cấu trúc Đồ thị vô hướng (Undirected Graph) mô hình hóa mạng xã hội.
 * - Đỉnh (Vertex/Node): Người dùng (User).
 * - Cạnh (Edge): Mối quan hệ kết bạn hai chiều giữa 2 người dùng.
 * - Cấu trúc lưu trữ: Danh sách kề biểu diễn bằng HashMap<String, Set<String>>
 *   (hoặc Map<String, List<String>> tương ứng với gợi ý đề bài).
 */
public class SocialGraph {
    // Lưu trữ thông tin đối tượng User theo ID
    private final Map<String, User> userMap;

    // Danh sách kề: userId -> Tập hợp các userId của bạn bè kết nối trực tiếp
    private final Map<String, Set<String>> adjacencyList;

    public SocialGraph() {
        this.userMap = new LinkedHashMap<>();
        this.adjacencyList = new LinkedHashMap<>();
    }

    /**
     * Thêm một người dùng mới vào mạng xã hội.
     * @param user Đối tượng người dùng
     * @return true nếu thêm thành công, false nếu ID người dùng đã tồn tại
     */
    public boolean addUser(User user) {
        if (user == null || user.getId() == null) {
            return false;
        }
        String id = user.getId().trim();
        if (userMap.containsKey(id)) {
            return false;
        }
        userMap.put(id, user);
        adjacencyList.putIfAbsent(id, new LinkedHashSet<>());
        return true;
    }

    /**
     * Xóa một người dùng khỏi mạng xã hội cùng toàn bộ kết nối liên quan.
     * @param userId ID người dùng cần xóa
     * @return true nếu xóa thành công, false nếu không tìm thấy người dùng
     */
    public boolean removeUser(String userId) {
        if (userId == null || !userMap.containsKey(userId)) {
            return false;
        }
        // Xóa ID này khỏi danh sách bạn bè của tất cả những người khác
        Set<String> friends = adjacencyList.get(userId);
        if (friends != null) {
            for (String friendId : new HashSet<>(friends)) {
                Set<String> theirFriends = adjacencyList.get(friendId);
                if (theirFriends != null) {
                    theirFriends.remove(userId);
                }
                User friendUser = userMap.get(friendId);
                if (friendUser != null) {
                    friendUser.removeFriend(userId);
                }
            }
        }
        adjacencyList.remove(userId);
        userMap.remove(userId);
        return true;
    }

    /**
     * Thiết lập mối quan hệ kết bạn hai chiều (Cạnh vô hướng) giữa 2 người dùng.
     * @param userId1 ID người dùng 1
     * @param userId2 ID người dùng 2
     * @return true nếu kết bạn thành công, false nếu không hợp lệ hoặc đã là bạn
     */
    public boolean addFriendship(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return false;
        userId1 = userId1.trim();
        userId2 = userId2.trim();

        if (userId1.equalsIgnoreCase(userId2)) {
            return false; // Không tự kết bạn với chính mình
        }

        if (!userMap.containsKey(userId1) || !userMap.containsKey(userId2)) {
            return false; // Một trong hai người không tồn tại
        }

        Set<String> friends1 = adjacencyList.get(userId1);
        Set<String> friends2 = adjacencyList.get(userId2);

        if (friends1.contains(userId2)) {
            return false; // Đã là bạn bè trước đó
        }

        friends1.add(userId2);
        friends2.add(userId1);

        userMap.get(userId1).addFriend(userId2);
        userMap.get(userId2).addFriend(userId1);
        return true;
    }

    /**
     * Hủy mối quan hệ bạn bè giữa 2 người dùng.
     */
    public boolean removeFriendship(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return false;
        userId1 = userId1.trim();
        userId2 = userId2.trim();

        if (!userMap.containsKey(userId1) || !userMap.containsKey(userId2)) {
            return false;
        }

        Set<String> friends1 = adjacencyList.get(userId1);
        Set<String> friends2 = adjacencyList.get(userId2);

        boolean removed1 = (friends1 != null) && friends1.remove(userId2);
        boolean removed2 = (friends2 != null) && friends2.remove(userId1);

        if (userMap.containsKey(userId1)) userMap.get(userId1).removeFriend(userId2);
        if (userMap.containsKey(userId2)) userMap.get(userId2).removeFriend(userId1);

        return removed1 || removed2;
    }

    /**
     * Kiểm tra xem 2 người dùng có phải là bạn bè trực tiếp hay không.
     */
    public boolean isFriend(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return false;
        Set<String> friends = adjacencyList.get(userId1.trim());
        return friends != null && friends.contains(userId2.trim());
    }

    public boolean containsUser(String userId) {
        return userId != null && userMap.containsKey(userId.trim());
    }

    public User getUser(String userId) {
        if (userId == null) return null;
        return userMap.get(userId.trim());
    }

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(userMap.values());
    }

    public Set<String> getAllUserIds() {
        return Collections.unmodifiableSet(userMap.keySet());
    }

    /**
     * Lấy danh sách ID bạn bè trực tiếp của một người dùng.
     */
    public Set<String> getFriendIds(String userId) {
        if (userId == null) return Collections.emptySet();
        Set<String> friends = adjacencyList.get(userId.trim());
        return friends != null ? Collections.unmodifiableSet(friends) : Collections.emptySet();
    }

    /**
     * Lấy danh sách các đối tượng User là bạn bè của một người dùng.
     */
    public List<User> getFriends(String userId) {
        Set<String> ids = getFriendIds(userId);
        List<User> list = new ArrayList<>();
        for (String id : ids) {
            User u = userMap.get(id);
            if (u != null) {
                list.add(u);
            }
        }
        return list;
    }

    /**
     * Lấy danh sách kề dạng Map<String, List<String>> theo chuẩn yêu cầu đề bài.
     */
    public Map<String, List<String>> getAdjacencyListAsMap() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : adjacencyList.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return result;
    }

    public int getUserCount() {
        return userMap.size();
    }

    /**
     * Tổng số cạnh (Mối quan hệ bạn bè hai chiều) trong đồ thị.
     */
    public int getEdgeCount() {
        int total = 0;
        for (Set<String> friends : adjacencyList.values()) {
            total += friends.size();
        }
        return total / 2; // Đồ thị vô hướng mỗi cạnh đếm 2 lần
    }

    /**
     * In toàn bộ đồ thị mạng xã hội ra Console dưới dạng danh sách kề.
     */
    public void displayGraph() {
        System.out.println("================================================================================");
        System.out.printf("   MẠNG XÃ HỘI (TỔNG CỘNG: %d NGƯỜI DÙNG, %d MỐI QUAN HỆ BẠN BÈ)\n", 
                getUserCount(), getEdgeCount());
        System.out.println("================================================================================");
        for (User user : userMap.values()) {
            Set<String> friends = adjacencyList.getOrDefault(user.getId(), Collections.emptySet());
            List<String> friendNames = new ArrayList<>();
            for (String fid : friends) {
                User f = userMap.get(fid);
                friendNames.add(f != null ? f.getName() + " (" + fid + ")" : fid);
            }
            System.out.printf("[%s] %-18s (%-4s) -> Bạn bè (%d): %s\n",
                    user.getId(),
                    user.getName(),
                    user.getMajor(),
                    friends.size(),
                    friendNames.isEmpty() ? "(Chưa có bạn bè)" : String.join(", ", friendNames));
        }
        System.out.println("================================================================================");
    }

    /**
     * Nạp bộ dữ liệu mẫu sinh động gồm sinh viên các khoa của PTIT.
     */
    public void loadSampleData() {
        userMap.clear();
        adjacencyList.clear();

        // Danh sách người dùng mẫu (Sinh viên PTIT)
        User u1 = new User("U01", "Nguyễn Minh Tiến", 22, "Nam", "CNTT", "Hà Nội");
        u1.addHobby("Lập trình"); u1.addHobby("AI"); u1.addHobby("Bóng đá");

        User u2 = new User("U02", "Đồng Duy Phúc", 22, "Nam", "CNTT", "Hải Phòng");
        u2.addHobby("Lập trình"); u2.addHobby("Đồ thị"); u2.addHobby("Âm nhạc");

        User u3 = new User("U03", "Trần Thu Hà", 21, "Nữ", "ATTT", "Bắc Ninh");
        u3.addHobby("Bảo mật"); u3.addHobby("Đọc sách"); u3.addHobby("Nhiếp ảnh");

        User u4 = new User("U04", "Lê Hoàng Long", 22, "Nam", "DTVT", "Nam Định");
        u4.addHobby("Điện tử"); u4.addHobby("Bóng rổ"); u4.addHobby("Game");

        User u5 = new User("U05", "Phạm Hải Đăng", 23, "Nam", "CNTT", "Hà Nam");
        u5.addHobby("Web Fullstack"); u5.addHobby("Cầu lông"); u5.addHobby("Lập trình");

        User u6 = new User("U06", "Vũ Mai Phương", 21, "Nữ", "Marketing", "Hà Nội");
        u6.addHobby("Content"); u6.addHobby("Du lịch"); u6.addHobby("Nhiếp ảnh");

        User u7 = new User("U07", "Đỗ Quang Huy", 22, "Nam", "ATTT", "Thái Bình");
        u7.addHobby("Mạng máy tính"); u7.addHobby("Gym"); u7.addHobby("Bóng đá");

        User u8 = new User("U08", "Ngô Quỳnh Chi", 20, "Nữ", "CNTT", "Quảng Ninh");
        u8.addHobby("UI/UX"); u8.addHobby("Vẽ"); u8.addHobby("Đọc sách");

        User u9 = new User("U09", "Bùi Quốc Anh", 22, "Nam", "DTVT", "Nghệ An");
        u9.addHobby("IoT"); u9.addHobby("Robotics"); u9.addHobby("Bóng rổ");

        User u10 = new User("U10", "Hoàng Kim Yến", 21, "Nữ", "Kế toán", "Phú Thọ");
        u10.addHobby("Tài chính"); u10.addHobby("Nấu ăn"); u10.addHobby("Du lịch");

        // Một nhóm cô lập nhỏ để kiểm thử thành phần liên thông
        User u11 = new User("U11", "Đặng Văn Nam", 23, "Nam", "QTKD", "Thanh Hóa");
        u11.addHobby("Kinh doanh"); u11.addHobby("Bơi lội");

        User u12 = new User("U12", "Lê Thúy Nga", 22, "Nữ", "QTKD", "Hải Dương");
        u12.addHobby("Khởi nghiệp"); u12.addHobby("Yoga");

        // Thêm vào đồ thị
        addUser(u1); addUser(u2); addUser(u3); addUser(u4);
        addUser(u5); addUser(u6); addUser(u7); addUser(u8);
        addUser(u9); addUser(u10); addUser(u11); addUser(u12);

        // Thiết lập mối quan hệ bạn bè (Cạnh vô hướng)
        // Nhóm chính (Thành phần liên thông 1)
        addFriendship("U01", "U02"); // Tiến <-> Phúc
        addFriendship("U01", "U03"); // Tiến <-> Hà
        addFriendship("U01", "U05"); // Tiến <-> Đăng
        addFriendship("U02", "U03"); // Phúc <-> Hà (Tiến và Phúc có bạn chung là Hà)
        addFriendship("U02", "U04"); // Phúc <-> Long
        addFriendship("U03", "U06"); // Hà <-> Phương
        addFriendship("U03", "U07"); // Hà <-> Huy
        addFriendship("U04", "U07"); // Long <-> Huy
        addFriendship("U04", "U09"); // Long <-> Quốc Anh
        addFriendship("U05", "U08"); // Đăng <-> Quỳnh Chi
        addFriendship("U06", "U08"); // Phương <-> Quỳnh Chi
        addFriendship("U06", "U10"); // Phương <-> Kim Yến
        addFriendship("U07", "U08"); // Huy <-> Quỳnh Chi
        addFriendship("U08", "U10"); // Quỳnh Chi <-> Kim Yến

        // Nhóm độc lập (Thành phần liên thông 2)
        addFriendship("U11", "U12"); // Nam <-> Nga
    }

    /**
     * Xuất danh sách người dùng và các liên kết bạn bè ra tệp văn bản.
     */
    public void exportToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.write("# SOCIAL_NETWORK_USERS\n");
            for (User u : userMap.values()) {
                String hobbies = String.join(";", u.getHobbies());
                writer.write(String.format("USER|%s|%s|%d|%s|%s|%s|%s\n",
                        u.getId(), u.getName(), u.getAge(), u.getGender(), 
                        u.getMajor(), u.getHometown(), hobbies));
            }
            writer.write("# SOCIAL_NETWORK_FRIENDSHIPS\n");
            Set<String> writtenPairs = new HashSet<>();
            for (Map.Entry<String, Set<String>> entry : adjacencyList.entrySet()) {
                String u1 = entry.getKey();
                for (String u2 : entry.getValue()) {
                    String pairKey = u1.compareTo(u2) < 0 ? u1 + "-" + u2 : u2 + "-" + u1;
                    if (!writtenPairs.contains(pairKey)) {
                        writtenPairs.add(pairKey);
                        writer.write(String.format("FRIEND|%s|%s\n", u1, u2));
                    }
                }
            }
        }
    }

    /**
     * Đọc đồ thị người dùng và bạn bè từ tệp văn bản.
     */
    public void importFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Không tìm thấy file: " + filePath);
        }
        userMap.clear();
        adjacencyList.clear();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|");
                if (parts[0].equalsIgnoreCase("USER") && parts.length >= 7) {
                    String id = parts[1];
                    String name = parts[2];
                    int age = Integer.parseInt(parts[3]);
                    String gender = parts[4];
                    String major = parts[5];
                    String hometown = parts[6];
                    User user = new User(id, name, age, gender, major, hometown);
                    if (parts.length >= 8 && !parts[7].isEmpty()) {
                        String[] hList = parts[7].split(";");
                        for (String h : hList) user.addHobby(h);
                    }
                    addUser(user);
                } else if (parts[0].equalsIgnoreCase("FRIEND") && parts.length >= 3) {
                    addFriendship(parts[1], parts[2]);
                }
            }
        }
    }
}
