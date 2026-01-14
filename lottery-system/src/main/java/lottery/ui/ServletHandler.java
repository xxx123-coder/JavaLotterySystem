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
import java.util.Map;

/**
 * HTTP请求处理器
 */
public class ServletHandler {
    private UserService userService;
    private TicketService ticketService;
    private LotteryService lotteryService;
    private PageGenerator pageGenerator;

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

        // 解析参数
        Map<String, String> params = new HashMap<>();
        if ("POST".equalsIgnoreCase(method)) {
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
                    response = pageGenerator.generateBuyTicketPage();
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
            case "/recharge":
                if ("POST".equalsIgnoreCase(method)) {
                    response = handleRecharge(params);
                } else {
                    response = pageGenerator.generateRechargePage();
                }
                break;
            case "/logout":
                response = handleLogout();
                contentType = "text/plain";
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
            // 登录成功，跳转到主页面
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
        // 简化处理，实际需要会话管理
        return pageGenerator.generateMainPage(null);
    }

    /**
     * 处理购买彩票
     */
    private String handleBuyTicket(Map<String, String> params) {
        try {
            int userId = Integer.parseInt(params.get("userId"));
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
            String winningNumbers = lotteryService.drawLottery();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("winningNumbers", winningNumbers);
            result.put("message", "抽奖完成！");
            return generateJsonResponse(result);
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
            int userId = Integer.parseInt(params.get("userId"));
            return pageGenerator.generateMyTicketsPage(ticketService.getUserTickets(userId));
        } catch (Exception e) {
            return generateErrorPage(e.getMessage());
        }
    }

    /**
     * 处理充值
     */
    private String handleRecharge(Map<String, String> params) {
        try {
            int userId = Integer.parseInt(params.get("userId"));
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
        // 清除会话等操作
        return "登出成功";
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
}