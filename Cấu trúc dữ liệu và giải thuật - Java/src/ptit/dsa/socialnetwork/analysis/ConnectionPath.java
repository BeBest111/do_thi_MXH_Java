package ptit.dsa.socialnetwork.analysis;

import ptit.dsa.socialnetwork.model.User;

import java.util.Collections;
import java.util.List;

/**
 * ============================================================================
 * Lớp lưu trữ kết quả tìm đường kết nối ngắn nhất giữa hai người dùng (BFS Shortest Path).
 * ============================================================================
 */
public class ConnectionPath {
    private final String sourceId;
    private final String targetId;
    private final boolean connected;
    private final int distance; // Số cạnh (Degrees of Separation)
    private final List<User> path;

    public ConnectionPath(String sourceId, String targetId, boolean connected, int distance, List<User> path) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.connected = connected;
        this.distance = distance;
        this.path = path != null ? path : Collections.emptyList();
    }

    public static ConnectionPath notConnected(String sourceId, String targetId) {
        return new ConnectionPath(sourceId, targetId, false, -1, Collections.emptyList());
    }

    public static ConnectionPath sameUser(User user) {
        return new ConnectionPath(user.getId(), user.getId(), true, 0, Collections.singletonList(user));
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public boolean isConnected() {
        return connected;
    }

    public int getDistance() {
        return distance;
    }

    public List<User> getPath() {
        return Collections.unmodifiableList(path);
    }

    public String getFormattedPath() {
        if (!connected) {
            return String.format("Không có đường kết nối nào giữa người dùng '%s' và '%s' (Khác thành phần liên thông).",
                    sourceId, targetId);
        }
        if (distance == 0) {
            return "Hai người dùng là cùng một người (Khoảng cách = 0).";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Khoảng cách kết nối: %d bước (Degrees of Separation = %d)\n", distance, distance));
        sb.append("Lộ trình kết nối: ");
        for (int i = 0; i < path.size(); i++) {
            User u = path.get(i);
            sb.append(String.format("%s (%s)", u.getName(), u.getId()));
            if (i < path.size() - 1) {
                sb.append(" --> ");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFormattedPath();
    }
}
