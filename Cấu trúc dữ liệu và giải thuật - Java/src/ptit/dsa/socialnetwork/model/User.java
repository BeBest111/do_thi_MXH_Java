package ptit.dsa.socialnetwork.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * ============================================================================
 * HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG - KHOA CÔNG NGHỆ THÔNG TIN
 * Môn học: Cấu trúc dữ liệu và giải thuật - Java
 * Đề tài: Phân tích mạng xã hội bằng đồ thị (Graph Traversal)
 * Phân công: Người 1 - Nguyễn Minh Tiến (B22DVCN308)
 * ============================================================================
 * Lớp đại diện cho một người dùng (Node) trong mạng xã hội.
 */
public class User {
    private String id;
    private String name;
    private int age;
    private String gender;
    private String major;
    private String hometown;
    private Set<String> hobbies;
    private Set<String> friendIds;

    public User(String id, String name) {
        this(id, name, 20, "Không xác định", "Công nghệ thông tin", "Hà Nội");
    }

    public User(String id, String name, int age, String gender, String major, String hometown) {
        this.id = Objects.requireNonNull(id, "ID không được null").trim();
        this.name = Objects.requireNonNull(name, "Tên không được null").trim();
        this.age = age;
        this.gender = gender;
        this.major = major;
        this.hometown = hometown;
        this.hobbies = new LinkedHashSet<>();
        this.friendIds = new LinkedHashSet<>();
    }

    // --- Quản lý thông tin bạn bè nội bộ ---
    public boolean addFriend(String friendId) {
        if (friendId != null && !friendId.equalsIgnoreCase(this.id)) {
            return this.friendIds.add(friendId);
        }
        return false;
    }

    public boolean removeFriend(String friendId) {
        return this.friendIds.remove(friendId);
    }

    public boolean hasFriend(String friendId) {
        return this.friendIds.contains(friendId);
    }

    public int getFriendCount() {
        return this.friendIds.size();
    }

    public Set<String> getFriendIds() {
        return Collections.unmodifiableSet(this.friendIds);
    }

    // --- Quản lý sở thích ---
    public void addHobby(String hobby) {
        if (hobby != null && !hobby.trim().isEmpty()) {
            this.hobbies.add(hobby.trim());
        }
    }

    public Set<String> getHobbies() {
        return Collections.unmodifiableSet(this.hobbies);
    }

    // --- Getters & Setters ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equalsIgnoreCase(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d tuổi, %s)", id, name, age, major);
    }

    public String toDetailString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ID: %-12s | Họ tên: %-22s | Tuổi: %-2d | Giới tính: %-6s\n", id, name, age, gender));
        sb.append(String.format("Ngành: %-15s | Quê quán: %-16s | Số bạn bè: %-3d\n", major, hometown, friendIds.size()));
        sb.append(String.format("Sở thích: %s", hobbies.isEmpty() ? "Chưa cập nhật" : String.join(", ", hobbies)));
        return sb.toString();
    }
}
