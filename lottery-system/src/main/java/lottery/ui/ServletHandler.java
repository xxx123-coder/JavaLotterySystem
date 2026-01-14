package lottery.ui;

import com.sun.net.httpserver.HttpExchange;
import lottery.service.UserService;
import lottery.service.TicketService;
import lottery.service.LotteryService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求处理器
 */
public class ServletHandler {
    private UserService userService;
    private TicketService ticketService;
    private LotteryService lotteryService;
    private PageGenerator pageGenerator;

    // 会话管理：当前登录用户ID
    private Integer currentUserId = null;

    // 会话管理：已登录用户信息缓存
    private Map<String, Integer> userSessions = new HashMap<>();

    /**
     * 构造函数
     */
    public ServletHandler() {
        this.pageGenerator = new PageGenerator();
    }

    /**
     * 设置用户服务
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * 设置彩票服务
     */
    public void setTicketService(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * 设置抽奖服务
     */
    public void setLotteryService(LotteryService lotteryService) {
        this.lotteryService = lotteryService;
    }

    /**
     * 处理HTTP请求
     */
    public void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        System.out.println("请求路径: " + path + ", 方法: " + method);

        // 解析所有参数（GET和POST）
        Map<String, String> params = new HashMap<>();
        if ("GET".equalsIgnoreCase(method)) {
            params = parseGetParams(exchange);
        } else if ("POST".equalsIgnoreCase(method)) {
            params = parsePostParams(exchange);
        }

        // 路由处理
        String response = "";
        String contentType = "text/html;charset=UTF-8";

        switch (path) {
            case "/":
            case "/login":
                if ("POST".equalsIgnoreCase(method)) {
                    response = handleLogin(params);
                } else {
                    response = pageGenerator.generateLoginPage(null);
                }
                break;
            case "/register":
                if ("POST".equalsIgnoreCase(method)) {
                    response = handleRegister(params);
                } else {
                    response = pageGenerator.generateRegisterPage(null);
                }
                break;
            case "/main":
                response = handleMain(params);
                break;
            case "/buy-ticket":
                if ("POST".equalsIgnoreCase(method)) {
                    response = handleBuyTicket(params);
                } else {
                    // 检查是否已登录
                    if (currentUserId == null) {
                        response = generateRedirectToLogin();
                    } else {
                        response = pageGenerator.generateBuyTicketPage(currentUserId);
                    }
                }
                break;
            case "/draw":
                if ("POST".equalsIgnoreCase(method)) {
                    response = handleDraw();
                } else {
                    response = pageGenerator.generateDrawPage();
                }
                break;
            case "/my-tickets":
                response = handleMyTickets(params);
                break;
            case "/check-winning": // 新增：中奖查询
                response = handleCheckWinning(params);
                break;
            case "/mark-read": // 新增：标记为已读
                if ("POST".equalsIgnoreCase(method)) {
                    response = handleMarkAsRead(params);
                }
                break;
            case "/recharge":
                if ("POST".equalsIgnoreCase(method)) {
                    response = handleRecharge(params);
                } else {
                    // 检查是否已登录
                    if (currentUserId == null) {
                        response = generateRedirectToLogin();
                    } else {
                        response = pageGenerator.generateRechargePage(currentUserId);
                    }
                }
                break;
            case "/logout":
                response = handleLogout();
                break;
            default:
                response = generateErrorPage("404 - 页面未找到");
                break;
        }

        // 发送响应
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, response.getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * 处理登录
     */
    private String handleLogin(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (username == null || password == null ||
                username.trim().isEmpty() || password.trim().isEmpty()) {
            return pageGenerator.generateLoginPage("用户名和密码不能为空");
        }

        Map<String, Object> user = userService.login(username, password);
        if (user != null) {
            // 登录成功，设置当前用户ID
            Integer userId = ((Number) user.get("id")).intValue();
            currentUserId = userId;
            System.out.println("用户登录成功: " + username + ", ID: " + userId);

            // 跳转到主页面
            return pageGenerator.generateMainPage(user);
        } else {
            return pageGenerator.generateLoginPage("用户名或密码错误");
        }
    }

    /**
     * 处理注册
     */
    private String handleRegister(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String phone = params.get("phone");

        if (username == null || password == null || phone == null ||
                username.trim().isEmpty() || password.trim().isEmpty() || phone.trim().isEmpty()) {
            return pageGenerator.generateRegisterPage("请填写所有必填项");
        }

        boolean success = userService.register(username, password, phone);
        if (success) {
            return pageGenerator.generateSuccessPage("注册成功！", "login");
        } else {
            return pageGenerator.generateRegisterPage("用户名已存在");
        }
    }

    /**
     * 处理主页面
     */
    private String handleMain(Map<String, String> params) {
        // 如果当前用户已登录，显示用户信息
        if (currentUserId != null) {
            Map<String, Object> user = userService.getUserInfo(currentUserId);
            if (user != null) {
                return pageGenerator.generateMainPage(user);
            }
        }
        // 否则显示无用户信息的主页
        return pageGenerator.generateMainPage(null);
    }

    /**
     * 处理购买彩票
     */
    private String handleBuyTicket(Map<String, String> params) {
        try {
            // 从当前会话获取用户ID
            if (currentUserId == null) {
                return generateRedirectToLogin();
            }

            int userId = currentUserId;
            String ticketType = params.get("ticketType");
            String numbers = params.get("numbers");
            int betCount = Integer.parseInt(params.get("betCount"));

            Map<String, Object> result = new HashMap<>();

            if ("manual".equals(ticketType)) {
                Map<String, Object> ticket = ticketService.buyManualTicket(userId, numbers, betCount);
                result.put("success", true);
                result.put("message", "购买成功！号码：" + ticket.get("numbers"));
            } else if ("random".equals(ticketType)) {
                Map<String, Object> ticket = ticketService.buyRandomTicket(userId, betCount);
                result.put("success", true);
                result.put("message", "购买成功！随机号码：" + ticket.get("numbers"));
            } else {
                result.put("success", false);
                result.put("message", "参数错误");
            }

            return generateJsonResponse(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return generateJsonResponse(result);
        }
    }

    /**
     * 处理抽奖
     */
    private String handleDraw() {
        try {
            Map<String, Object> drawResult = lotteryService.drawLottery();
            return generateJsonResponse(drawResult);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return generateJsonResponse(result);
        }
    }

    /**
     * 处理我的彩票
     */
    private String handleMyTickets(Map<String, String> params) {
        try {
            // 从当前会话获取用户ID
            if (currentUserId == null) {
                return generateRedirectToLogin();
            }

            int userId = currentUserId;
            List<Map<String, Object>> userTickets = ticketService.getUserTickets(userId);
            return pageGenerator.generateMyTicketsPage(userTickets);
        } catch (Exception e) {
            return generateErrorPage(e.getMessage());
        }
    }

    /**
     * 处理中奖查询（新增）
     */
    private String handleCheckWinning(Map<String, String> params) {
        try {
            // 从当前会话获取用户ID
            if (currentUserId == null) {
                return generateRedirectToLogin();
            }

            int userId = currentUserId;
            List<Map<String, Object>> allWinnings = userService.getUserWinnings(userId);
            List<Map<String, Object>> unreadWinnings = lotteryService.getUserWinningNotifications(userId);

            return pageGenerator.generateWinningPage(allWinnings, unreadWinnings);
        } catch (Exception e) {
            return generateErrorPage(e.getMessage());
        }
    }

    /**
     * 处理标记为已读（新增）
     */
    private String handleMarkAsRead(Map<String, String> params) {
        try {
            // 从当前会话获取用户ID
            if (currentUserId == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "用户未登录");
                return generateJsonResponse(result);
            }

            int userId = currentUserId;
            boolean success = userService.markNotificationsAsRead(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "标记成功" : "标记失败");
            return generateJsonResponse(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return generateJsonResponse(result);
        }
    }

    /**
     * 处理充值
     */
    private String handleRecharge(Map<String, String> params) {
        try {
            // 从当前会话获取用户ID
            if (currentUserId == null) {
                return generateRedirectToLogin();
            }

            int userId = currentUserId;
            double amount = Double.parseDouble(params.get("amount"));

            boolean success = userService.recharge(userId, amount);
            Map<String, Object> result = new HashMap<>();

            if (success) {
                result.put("success", true);
                result.put("message", "充值成功！");
            } else {
                result.put("success", false);
                result.put("message", "充值失败");
            }

            return generateJsonResponse(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return generateJsonResponse(result);
        }
    }

    /**
     * 处理登出
     */
    private String handleLogout() {
        System.out.println("用户退出登录: ID=" + currentUserId);
        currentUserId = null;

        // 生成重定向到登录页面的HTML
        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>退出登录</title>" +
                "<script>" +
                "setTimeout(function() {" +
                "  window.location.href = '/login';" +
                "}, 1500);" +
                "</script>" +
                "</head>" +
                "<body>" +
                "<h1>退出登录成功</h1>" +
                "<p>正在跳转到登录页面...</p>" +
                "</body>" +
                "</html>";
        return html;
    }

    /**
     * 解析POST参数
     */
    private Map<String, String> parsePostParams(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<>();

        InputStream is = exchange.getRequestBody();
        String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (requestBody.contains("&")) {
            String[] pairs = requestBody.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    params.put(key, value);
                }
            }
        }

        return params;
    }

    /**
     * 解析GET参数
     */
    private Map<String, String> parseGetParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();

        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    try {
                        String key = URLDecoder.decode(keyValue[0], "UTF-8");
                        String value = URLDecoder.decode(keyValue[1], "UTF-8");
                        params.put(key, value);
                    } catch (Exception e) {
                        // 忽略解码错误
                    }
                }
            }
        }

        return params;
    }

    /**
     * 生成JSON响应
     */
    private String generateJsonResponse(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else {
                json.append("\"").append(value).append("\"");
            }

            first = false;
        }

        json.append("}");
        return json.toString();
    }

    /**
     * 转义JSON字符串
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 生成错误页面
     */
    private String generateErrorPage(String message) {
        return pageGenerator.generateErrorPage(message);
    }

    /**
     * 生成重定向到登录页面的响应
     */
    private String generateRedirectToLogin() {
        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>需要登录</title>" +
                "<script>" +
                "alert('请先登录系统！');" +
                "window.location.href = '/login';" +
                "</script>" +
                "</head>" +
                "<body>" +
                "<p>正在跳转到登录页面...</p>" +
                "</body>" +
                "</html>";
        return html;
    }
}