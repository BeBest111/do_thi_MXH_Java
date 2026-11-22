package ptit.dsa.socialnetwork.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ptit.dsa.socialnetwork.analysis.ConnectionPath;
import ptit.dsa.socialnetwork.analysis.FriendRecommendation;
import ptit.dsa.socialnetwork.analysis.SocialAnalyzer;
import ptit.dsa.socialnetwork.graph.SocialGraph;
import ptit.dsa.socialnetwork.model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * ============================================================================
 * REST API + Static file server cho giao diện Web (không cần thư viện ngoài,
 * chỉ dùng com.sun.net.httpserver có sẵn trong JDK).
 * ----------------------------------------------------------------------------
 *   GET    /api/graph                          Toàn bộ đồ thị + thống kê
 *   POST   /api/users                          Thêm người dùng (form-urlencoded)
 *   DELETE /api/users/{id}                     Xóa người dùng
 *   POST   /api/friendships   (a, b)           Thêm quan hệ bạn bè
 *   DELETE /api/friendships?a=&b=              Hủy quan hệ bạn bè
 *   GET    /api/mutual?a=&b=                   Bạn bè chung
 *   GET    /api/recommend?user=&algo=bfs|dfs&depth=&limit=   Gợi ý bạn mới
 *   GET    /api/path?from=&to=                 Đường kết nối ngắn nhất (BFS)
 *   GET    /api/influencers?k=                 Top người có tầm ảnh hưởng
 *   GET    /api/jaccard?a=&b=                  Độ tương đồng Jaccard
 *   POST   /api/reset                          Nạp lại dữ liệu mẫu
 * Mọi thao tác ghi được đồng bộ hóa (synchronized) để an toàn khi nhiều request.
 * ============================================================================
 */
public class ApiServer {
    private final SocialGraph graph = new SocialGraph();
    private final SocialAnalyzer analyzer = new SocialAnalyzer(graph);
    private final Path webRoot;

    public ApiServer(Path webRoot) {
        this.webRoot = webRoot.toAbsolutePath().normalize();
        graph.loadSampleData();
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        String web = args.length > 1 ? args[1] : "web";
        ApiServer app = new ApiServer(Paths.get(web));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", app::handle);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("=====================================================");
        System.out.println(" Social Network Web đang chạy: http://localhost:" + port);
        System.out.println(" Thư mục giao diện: " + app.webRoot);
        System.out.println(" Nhấn Ctrl+C để dừng.");
        System.out.println("=====================================================");
    }

    // ------------------------------------------------------------------ routing
    private void handle(HttpExchange ex) throws IOException {
        try {
            addCors(ex.getResponseHeaders());
            String method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) {
                ex.sendResponseHeaders(204, -1);
                ex.close();
                return;
            }
            String path = ex.getRequestURI().getPath();
            if (path.startsWith("/api/")) {
                Map<String, String> params = parseParams(ex.getRequestURI().getRawQuery());
                if ("POST".equals(method) || "PUT".equals(method)) {
                    String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    params.putAll(parseParams(body));
                }
                String json;
                synchronized (this) {
                    json = route(method, path, params);
                }
                if (json == null) {
                    sendJson(ex, 404, error("Không tìm thấy API: " + method + " " + path));
                } else {
                    sendJson(ex, 200, json);
                }
            } else {
                serveStatic(ex, path);
            }
        } catch (ApiException e) {
            sendJson(ex, e.status, error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(ex, 500, error("Lỗi máy chủ: " + e));
        }
    }

    private String route(String method, String path, Map<String, String> p) {
        switch (method + " " + path) {
            case "GET /api/graph":        return graphJson();
            case "POST /api/users":       return addUser(p);
            case "POST /api/friendships": return addFriendship(p);
            case "DELETE /api/friendships": return removeFriendship(p);
            case "GET /api/mutual":       return mutual(p);
            case "GET /api/recommend":    return recommend(p);
            case "GET /api/path":         return shortestPath(p);
            case "GET /api/influencers":  return influencers(p);
            case "GET /api/jaccard":      return jaccard(p);
            case "POST /api/reset":
                resetGraph();
                return graphJson();
            default:
                if ("DELETE".equals(method) && path.startsWith("/api/users/")) {
                    String id = URLDecoder.decode(path.substring("/api/users/".length()), StandardCharsets.UTF_8);
                    requireUser(id);
                    graph.removeUser(id);
                    return graphJson();
                }
                return null;
        }
    }

    // ------------------------------------------------------------------ handlers
    private void resetGraph() {
        for (String id : new ArrayList<>(graph.getAllUserIds())) graph.removeUser(id);
        graph.loadSampleData();
    }

    private String addUser(Map<String, String> p) {
        String name = req(p, "name");
        String id = p.getOrDefault("id", "").trim();
        if (id.isEmpty()) {
            int n = graph.getUserCount() + 1;
            do { id = String.format("U%02d", n++); } while (graph.containsUser(id));
        }
        int age = 20;
        try { age = Integer.parseInt(p.getOrDefault("age", "20").trim()); } catch (NumberFormatException ignored) {}
        if (age < 1 || age > 120) throw new ApiException(400, "Tuổi không hợp lệ");
        User u = new User(id, name.trim(), age,
                p.getOrDefault("gender", "Khác"), p.getOrDefault("major", "CNTT"),
                p.getOrDefault("hometown", "Hà Nội"));
        for (String h : p.getOrDefault("hobbies", "").split("[,;]")) u.addHobby(h);
        if (!graph.addUser(u)) throw new ApiException(409, "ID '" + id + "' đã tồn tại");
        return graphJson();
    }

    private String addFriendship(Map<String, String> p) {
        String a = req(p, "a"), b = req(p, "b");
        requireUser(a);
        requireUser(b);
        if (a.trim().equals(b.trim())) throw new ApiException(400, "Không thể tự kết bạn với chính mình");
        if (!graph.addFriendship(a, b)) throw new ApiException(409, "Hai người đã là bạn bè");
        return graphJson();
    }

    private String removeFriendship(Map<String, String> p) {
        String a = req(p, "a"), b = req(p, "b");
        requireUser(a);
        requireUser(b);
        if (!graph.removeFriendship(a, b)) throw new ApiException(409, "Hai người chưa là bạn bè");
        return graphJson();
    }

    private String mutual(Map<String, String> p) {
        String a = req(p, "a"), b = req(p, "b");
        requireUser(a);
        requireUser(b);
        List<User> list = analyzer.getMutualFriends(a, b);
        return "{\"a\":" + str(a) + ",\"b\":" + str(b) + ",\"count\":" + list.size()
                + ",\"jaccard\":" + num(analyzer.calculateJaccardSimilarity(a, b))
                + ",\"mutual\":" + usersJson(list) + "}";
    }

    private String recommend(Map<String, String> p) {
        String user = req(p, "user");
        requireUser(user);
        String algo = p.getOrDefault("algo", "bfs").toLowerCase();
        int limit = intParam(p, "limit", 10);
        List<FriendRecommendation> recs;
        if ("dfs".equals(algo)) {
            recs = analyzer.recommendFriendsDFS(user, intParam(p, "depth", 3), limit);
        } else {
            algo = "bfs";
            recs = analyzer.recommendFriendsBFS(user, limit);
        }
        StringBuilder sb = new StringBuilder("{\"user\":").append(str(user))
                .append(",\"algo\":").append(str(algo)).append(",\"results\":[");
        for (int i = 0; i < recs.size(); i++) {
            FriendRecommendation r = recs.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"user\":").append(userJson(r.getUser()))
              .append(",\"mutualCount\":").append(r.getMutualFriendsCount())
              .append(",\"score\":").append(num(r.getSimilarityScore()))
              .append(",\"distance\":").append(r.getDistance())
              .append(",\"mutual\":").append(usersJson(r.getMutualFriends())).append('}');
        }
        return sb.append("]}").toString();
    }

    private String shortestPath(Map<String, String> p) {
        String from = req(p, "from"), to = req(p, "to");
        requireUser(from);
        requireUser(to);
        ConnectionPath cp = analyzer.findShortestPathBFS(from, to);
        return "{\"connected\":" + cp.isConnected() + ",\"distance\":" + cp.getDistance()
                + ",\"path\":" + usersJson(cp.getPath()) + "}";
    }

    private String influencers(Map<String, String> p) {
        return usersJson(analyzer.getTopInfluencers(intParam(p, "k", 5)));
    }

    private String jaccard(Map<String, String> p) {
        String a = req(p, "a"), b = req(p, "b");
        requireUser(a);
        requireUser(b);
        return "{\"jaccard\":" + num(analyzer.calculateJaccardSimilarity(a, b)) + "}";
    }

    private String graphJson() {
        StringBuilder sb = new StringBuilder("{\"nodes\":[");
        boolean first = true;
        for (User u : graph.getAllUsers()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(userJson(u));
        }
        sb.append("],\"edges\":[");
        first = true;
        Set<String> seen = new HashSet<>();
        for (User u : graph.getAllUsers()) {
            for (String f : graph.getFriendIds(u.getId())) {
                String key = u.getId().compareTo(f) < 0 ? u.getId() + "|" + f : f + "|" + u.getId();
                if (seen.add(key)) {
                    if (!first) sb.append(',');
                    first = false;
                    sb.append('[').append(str(u.getId())).append(',').append(str(f)).append(']');
                }
            }
        }
        List<List<User>> comps = analyzer.findConnectedComponents();
        sb.append("],\"components\":[");
        for (int i = 0; i < comps.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('[');
            for (int j = 0; j < comps.get(i).size(); j++) {
                if (j > 0) sb.append(',');
                sb.append(str(comps.get(i).get(j).getId()));
            }
            sb.append(']');
        }
        sb.append("],\"stats\":{\"users\":").append(graph.getUserCount())
          .append(",\"edges\":").append(graph.getEdgeCount())
          .append(",\"components\":").append(comps.size()).append("}}");
        return sb.toString();
    }

    // ------------------------------------------------------------------ JSON helpers
    private String userJson(User u) {
        StringBuilder sb = new StringBuilder("{\"id\":").append(str(u.getId()))
                .append(",\"name\":").append(str(u.getName()))
                .append(",\"age\":").append(u.getAge())
                .append(",\"gender\":").append(str(u.getGender()))
                .append(",\"major\":").append(str(u.getMajor()))
                .append(",\"hometown\":").append(str(u.getHometown()))
                .append(",\"degree\":").append(u.getFriendCount())
                .append(",\"hobbies\":[");
        boolean first = true;
        for (String h : u.getHobbies()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(str(h));
        }
        return sb.append("]}").toString();
    }

    private String usersJson(Collection<User> users) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (User u : users) {
            if (!first) sb.append(',');
            first = false;
            sb.append(userJson(u));
        }
        return sb.append(']').toString();
    }

    private static String str(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    private static String num(double d) {
        return String.format(Locale.ROOT, "%.4f", d);
    }

    private static String error(String msg) {
        return "{\"error\":" + str(msg) + "}";
    }

    // ------------------------------------------------------------------ misc helpers
    private void requireUser(String id) {
        if (!graph.containsUser(id)) throw new ApiException(404, "Không tìm thấy người dùng '" + id + "'");
    }

    private static String req(Map<String, String> p, String key) {
        String v = p.get(key);
        if (v == null || v.trim().isEmpty()) throw new ApiException(400, "Thiếu tham số '" + key + "'");
        return v.trim();
    }

    private static int intParam(Map<String, String> p, String key, int def) {
        try { return Integer.parseInt(p.getOrDefault(key, "").trim()); } catch (NumberFormatException e) { return def; }
    }

    private static Map<String, String> parseParams(String raw) {
        Map<String, String> map = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) return map;
        for (String pair : raw.split("&")) {
            if (pair.isEmpty()) continue;
            int i = pair.indexOf('=');
            String k = i < 0 ? pair : pair.substring(0, i);
            String v = i < 0 ? "" : pair.substring(i + 1);
            map.put(URLDecoder.decode(k, StandardCharsets.UTF_8), URLDecoder.decode(v, StandardCharsets.UTF_8));
        }
        return map;
    }

    private static void addCors(Headers h) {
        h.set("Access-Control-Allow-Origin", "*");
        h.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        h.set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private void serveStatic(HttpExchange ex, String path) throws IOException {
        if (path.equals("/")) path = "/index.html";
        Path file = webRoot.resolve("." + URLDecoder.decode(path, StandardCharsets.UTF_8)).normalize();
        // Chặn path traversal (../)
        if (!file.startsWith(webRoot) || !Files.isRegularFile(file)) {
            byte[] msg = "404 Not Found".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(404, msg.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(msg); }
            return;
        }
        byte[] data = Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", mime(file.toString()));
        ex.getResponseHeaders().set("Cache-Control", "no-cache");
        ex.sendResponseHeaders(200, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }

    private static String mime(String name) {
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".css"))  return "text/css; charset=utf-8";
        if (name.endsWith(".js"))   return "application/javascript; charset=utf-8";
        if (name.endsWith(".svg"))  return "image/svg+xml";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }

    private static class ApiException extends RuntimeException {
        final int status;
        ApiException(int status, String msg) { super(msg); this.status = status; }
    }
}
