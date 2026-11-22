package ptit.dsa.socialnetwork.analysis;

import ptit.dsa.socialnetwork.model.User;

import java.util.Collections;
import java.util.List;

/**
 * ============================================================================
 * Lớp lưu trữ kết quả gợi ý kết bạn cho một người dùng.
 * Bao gồm đối tượng người dùng được gợi ý, số lượng bạn chung, danh sách bạn chung,
 * độ tương đồng và điểm số đánh giá.
 * ============================================================================
 */
public class FriendRecommendation implements Comparable<FriendRecommendation> {
    private final User user;
    private final int mutualFriendsCount;
    private final List<User> mutualFriends;
    private final double similarityScore;
    private final String algorithmUsed; // "BFS" hoặc "DFS"
    private final int distance;         // Khoảng cách (số cạnh) từ người gốc đến ứng viên

    public FriendRecommendation(User user, int mutualFriendsCount, List<User> mutualFriends, 
                                double similarityScore, String algorithmUsed) {
        this(user, mutualFriendsCount, mutualFriends, similarityScore, algorithmUsed, -1);
    }

    public FriendRecommendation(User user, int mutualFriendsCount, List<User> mutualFriends, 
                                double similarityScore, String algorithmUsed, int distance) {
        this.distance = distance;
        this.user = user;
        this.mutualFriendsCount = mutualFriendsCount;
        this.mutualFriends = mutualFriends != null ? mutualFriends : Collections.emptyList();
        this.similarityScore = similarityScore;
        this.algorithmUsed = algorithmUsed;
    }

    public User getUser() {
        return user;
    }

    public int getMutualFriendsCount() {
        return mutualFriendsCount;
    }

    public List<User> getMutualFriends() {
        return Collections.unmodifiableList(mutualFriends);
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public int getDistance() {
        return distance;
    }

    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    @Override
    public int compareTo(FriendRecommendation o) {
        // Ưu tiên số bạn chung cao hơn, nếu bằng nhau thì so điểm tương đồng
        int cmp = Integer.compare(o.mutualFriendsCount, this.mutualFriendsCount);
        if (cmp != 0) return cmp;
        cmp = Double.compare(o.similarityScore, this.similarityScore);
        if (cmp != 0) return cmp;
        // Tie-break theo ID để kết quả luôn xác định (deterministic)
        return this.user.getId().compareTo(o.user.getId());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] %-20s | Số bạn chung: %-2d | Thuật toán: %s\n",
                user.getId(), user.getName(), mutualFriendsCount, algorithmUsed));
        if (!mutualFriends.isEmpty()) {
            sb.append("   -> Bạn chung: ");
            for (int i = 0; i < mutualFriends.size(); i++) {
                sb.append(mutualFriends.get(i).getName());
                if (i < mutualFriends.size() - 1) sb.append(", ");
            }
        }
        return sb.toString();
    }
}
