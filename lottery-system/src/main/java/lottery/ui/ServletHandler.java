package lottery.ui; // 定义包路径，表明该类属于lottery.ui包，负责HTTP请求处理

// 导入必要的类和包
import com.sun.net.httpserver.HttpExchange; // 导入HTTP交换类，用于处理HTTP请求和响应
import lottery.service.UserService; // 导入用户服务类
import lottery.service.TicketService; // 导入彩票服务类
import lottery.service.LotteryService; // 导入抽奖服务类

import java.io.IOException; // 导入输入输出异常类
import java.io.InputStream; // 导入输入流类
import java.io.OutputStream; // 导入输出流类
import java.net.URLDecoder; // 导入URL解码类
import java.nio.charset.StandardCharsets; // 导入标准字符集类
import java.util.HashMap; // 导入HashMap类
import java.util.List; // 导入List接口
import java.util.Map; // 导入Map接口

/**
 * HTTP请求处理器
 * 负责解析HTTP请求，调用相应服务，生成HTTP响应
 */
public class ServletHandler {
    private UserService userService; // 用户服务实例
    private TicketService ticketService; // 彩票服务实例
    private LotteryService lotteryService; // 抽奖服务实例
    private PageGenerator pageGenerator; // 页面生成器实例

    // 会话管理：当前登录用户ID
    private Integer currentUserId = null; // 当前用户ID，null表示未登录

    // 会话管理：已登录用户信息缓存
    private Map<String, Integer> userSessions = new HashMap<>(); // 用户会话缓存，key为会话标识，value为用户ID

    /**
     * 构造函数
     * 初始化页面生成器
     */
    public ServletHandler() {
        this.pageGenerator = new PageGenerator(); // 创建页面生成器实例
    }

    /**
     * 设置用户服务
     * @param userService 用户服务实例
     */
    public void setUserService(UserService userService) {
        this.userService = userService; // 设置用户服务
    }

    /**
     * 设置彩票服务
     * @param ticketService 彩票服务实例
     */
    public void setTicketService(TicketService ticketService) {
        this.ticketService = ticketService; // 设置彩票服务
    }

    /**
     * 设置抽奖服务
     * @param lotteryService 抽奖服务实例
     */
    public void setLotteryService(LotteryService lotteryService) {
        this.lotteryService = lotteryService; // 设置抽奖服务
    }

    /**
     * 处理HTTP请求
     * @param exchange HTTP交换对象，包含请求和响应信息
     * @throws IOException 输入输出异常
     */
    public void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath(); // 获取请求路径
        String method = exchange.getRequestMethod(); // 获取请求方法（GET、POST等）

        System.out.println("请求路径: " + path + ", 方法: " + method); // 输出请求日志

        // 解析所有参数（GET和POST）
        Map<String, String> params = new HashMap<>(); // 创建参数Map
        if ("GET".equalsIgnoreCase(method)) { // 如果是GET请求
            params = parseGetParams(exchange); // 解析GET参数
        } else if ("POST".equalsIgnoreCase(method)) { // 如果是POST请求
            params = parsePostParams(exchange); // 解析POST参数
        }

        // 路由处理
        String response = ""; // 响应内容
        String contentType = "text/html;charset=UTF-8"; // 响应内容类型，默认为HTML

        switch (path) { // 根据路径进行路由
            case "/": // 根路径
            case "/login": // 登录路径
                if ("POST".equalsIgnoreCase(method)) { // 如果是POST请求
                    response = handleLogin(params); // 处理登录
                } else { // 如果是GET请求
                    response = pageGenerator.generateLoginPage(null); // 生成登录页面
                }
                break;
            case "/register": // 注册路径
                if ("POST".equalsIgnoreCase(method)) { // 如果是POST请求
                    response = handleRegister(params); // 处理注册
                } else { // 如果是GET请求
                    response = pageGenerator.generateRegisterPage(null); // 生成注册页面
                }
                break;
            case "/main": // 主页面路径
                response = handleMain(params); // 处理主页面
                break;
            case "/buy-ticket": // 购买彩票路径
                if ("POST".equalsIgnoreCase(method)) { // 如果是POST请求
                    response = handleBuyTicket(params); // 处理购买彩票
                } else { // 如果是GET请求
                    // 检查是否已登录
                    if (currentUserId == null) { // 如果未登录
                        response = generateRedirectToLogin(); // 生成重定向到登录页面的响应
                    } else { // 如果已登录
                        response = pageGenerator.generateBuyTicketPage(currentUserId); // 生成购票页面
                    }
                }
                break;
            case "/draw": // 抽奖路径
                if ("POST".equalsIgnoreCase(method)) { // 如果是POST请求
                    response = handleDraw(); // 处理抽奖
                } else { // 如果是GET请求
                    response = pageGenerator.generateDrawPage(); // 生成抽奖页面
                }
                break;
            case "/my-tickets": // 我的彩票路径
                response = handleMyTickets(params); // 处理我的彩票
                break;
            case "/check-winning": // 新增：中奖查询路径
                response = handleCheckWinning(params); // 处理中奖查询
                break;
            case "/mark-read": // 新增：标记为已读路径
                if ("POST".equalsIgnoreCase(method)) { // 如果是POST请求
                    response = handleMarkAsRead(params); // 处理标记为已读
                }
                break;
            case "/recharge": // 充值路径
                if ("POST".equalsIgnoreCase(method)) { // 如果是POST请求
                    response = handleRecharge(params); // 处理充值
                } else { // 如果是GET请求
                    // 检查是否已登录
                    if (currentUserId == null) { // 如果未登录
                        response = generateRedirectToLogin(); // 生成重定向到登录页面的响应
                    } else { // 如果已登录
                        response = pageGenerator.generateRechargePage(currentUserId); // 生成充值页面
                    }
                }
                break;
            case "/logout": // 退出登录路径
                response = handleLogout(); // 处理登出
                break;
            default: // 默认路径（404）
                response = generateErrorPage("404 - 页面未找到"); // 生成错误页面
                break;
        }

        // 发送响应
        exchange.getResponseHeaders().set("Content-Type", contentType); // 设置响应头中的内容类型
        exchange.sendResponseHeaders(200, response.getBytes().length); // 发送响应头，状态码200，响应体长度

        OutputStream os = exchange.getResponseBody(); // 获取响应输出流
        os.write(response.getBytes()); // 写入响应内容
        os.close(); // 关闭输出流
    }

    /**
     * 处理登录
     * @param params 参数Map
     * @return String 响应内容
     */
    private String handleLogin(Map<String, String> params) {
        String username = params.get("username"); // 获取用户名参数
        String password = params.get("password"); // 获取密码参数

        if (username == null || password == null ||
                username.trim().isEmpty() || password.trim().isEmpty()) { // 如果用户名或密码为空
            return pageGenerator.generateLoginPage("用户名和密码不能为空"); // 生成登录页面并显示错误消息
        }

        Map<String, Object> user = userService.login(username, password); // 调用用户服务登录
        if (user != null) { // 如果登录成功
            // 登录成功，设置当前用户ID
            Integer userId = ((Number) user.get("id")).intValue(); // 从用户信息中获取用户ID
            currentUserId = userId; // 设置当前用户ID
            System.out.println("用户登录成功: " + username + ", ID: " + userId); // 输出登录成功日志

            // 跳转到主页面
            return pageGenerator.generateMainPage(user); // 生成主页面，并传递用户信息
        } else { // 如果登录失败
            return pageGenerator.generateLoginPage("用户名或密码错误"); // 生成登录页面并显示错误消息
        }
    }

    /**
     * 处理注册
     * @param params 参数Map
     * @return String 响应内容
     */
    private String handleRegister(Map<String, String> params) {
        String username = params.get("username"); // 获取用户名参数
        String password = params.get("password"); // 获取密码参数
        String phone = params.get("phone"); // 获取电话参数

        if (username == null || password == null || phone == null ||
                username.trim().isEmpty() || password.trim().isEmpty() || phone.trim().isEmpty()) { // 如果任何参数为空
            return pageGenerator.generateRegisterPage("请填写所有必填项"); // 生成注册页面并显示错误消息
        }

        boolean success = userService.register(username, password, phone); // 调用用户服务注册
        if (success) { // 如果注册成功
            return pageGenerator.generateSuccessPage("注册成功！", "login"); // 生成成功页面，并跳转到登录页面
        } else { // 如果注册失败（用户名已存在）
            return pageGenerator.generateRegisterPage("用户名已存在"); // 生成注册页面并显示错误消息
        }
    }

    /**
     * 处理主页面
     * @param params 参数Map
     * @return String 响应内容
     */
    private String handleMain(Map<String, String> params) {
        // 如果当前用户已登录，显示用户信息
        if (currentUserId != null) { // 如果当前用户ID不为空（已登录）
            Map<String, Object> user = userService.getUserInfo(currentUserId); // 获取用户信息
            if (user != null) { // 如果用户信息不为空
                return pageGenerator.generateMainPage(user); // 生成主页面并传递用户信息
            }
        }
        // 否则显示无用户信息的主页
        return pageGenerator.generateMainPage(null); // 生成主页面，不传递用户信息
    }

    /**
     * 处理购买彩票
     * @param params 参数Map
     * @return String 响应内容（JSON格式）
     */
    private String handleBuyTicket(Map<String, String> params) {
        try {
            // 从当前会话获取用户ID
            if (currentUserId == null) { // 如果未登录
                return generateRedirectToLogin(); // 生成重定向到登录页面的响应
            }

            int userId = currentUserId; // 获取当前用户ID
            String ticketType = params.get("ticketType"); // 获取彩票类型参数
            String numbers = params.get("numbers"); // 获取号码参数
            int betCount = Integer.parseInt(params.get("betCount")); // 获取注数参数并转换为整数

            Map<String, Object> result = new HashMap<>(); // 创建结果Map

            if ("manual".equals(ticketType)) { // 如果是手动选号
                Map<String, Object> ticket = ticketService.buyManualTicket(userId, numbers, betCount); // 调用彩票服务购买手动彩票
                result.put("success", true); // 设置成功标志为true
                result.put("message", "购买成功！号码：" + ticket.get("numbers")); // 设置成功消息
            } else if ("random".equals(ticketType)) { // 如果是随机选号
                Map<String, Object> ticket = ticketService.buyRandomTicket(userId, betCount); // 调用彩票服务购买随机彩票
                result.put("success", true); // 设置成功标志为true
                result.put("message", "购买成功！随机号码：" + ticket.get("numbers")); // 设置成功消息
            } else { // 如果彩票类型参数错误
                result.put("success", false); // 设置成功标志为false
                result.put("message", "参数错误"); // 设置错误消息
            }

            return generateJsonResponse(result); // 生成JSON响应
        } catch (Exception e) { // 捕获异常
            Map<String, Object> result = new HashMap<>(); // 创建结果Map
            result.put("success", false); // 设置成功标志为false
            result.put("message", e.getMessage()); // 设置异常消息
            return generateJsonResponse(result); // 生成JSON响应
        }
    }

    /**
     * 处理抽奖
     * @return String 响应内容（JSON格式）
     */
    private String handleDraw() {
        try {
            Map<String, Object> drawResult = lotteryService.drawLottery(); // 调用抽奖服务执行抽奖
            return generateJsonResponse(drawResult); // 生成JSON响应
        } catch (Exception e) { // 捕获异常
            Map<String, Object> result = new HashMap<>(); // 创建结果Map
            result.put("success", false); // 设置成功标志为false
            result.put("message", e.getMessage()); // 设置异常消息
            return generateJsonResponse(result); // 生成JSON响应
        }
    }

    /**
     * 处理我的彩票
     * @param params 参数Map
     * @return String 响应内容
     */
    private String handleMyTickets(Map<String, String> params) {
        try {
            // 从当前会话获取用户ID
            if (currentUserId == null) { // 如果未登录
                return generateRedirectToLogin(); // 生成重定向到登录页面的响应
            }

            int userId = currentUserId; // 获取当前用户ID
            List<Map<String, Object>> userTickets = ticketService.getUserTickets(userId); // 调用彩票服务获取用户彩票
            return pageGenerator.generateMyTicketsPage(userTickets); // 生成我的彩票页面
        } catch (Exception e) { // 捕获异常
            return generateErrorPage(e.getMessage()); // 生成错误页面
        }
    }

    /**
     * 处理中奖查询（新增）
     * @param params 参数Map
     * @return String 响应内容
     */
    private String handleCheckWinning(Map<String, String> params) {
        try {
            // 从当前会话获取用户ID
            if (currentUserId == null) { // 如果未登录
                return generateRedirectToLogin(); // 生成重定向到登录页面的响应
            }

            int userId = currentUserId; // 获取当前用户ID
            List<Map<String, Object>> allWinnings = userService.getUserWinnings(userId); // 调用用户服务获取所有中奖记录
            List<Map<String, Object>> unreadWinnings = lotteryService.getUserWinningNotifications(userId); // 调用抽奖服务获取未读中奖通知

            return pageGenerator.generateWinningPage(allWinnings, unreadWinnings); // 生成中奖查询页面
        } catch (Exception e) { // 捕获异常
            return generateErrorPage(e.getMessage()); // 生成错误页面
        }
    }

    /**
     * 处理标记为已读（新增）
     * @param params 参数Map
     * @return String 响应内容（JSON格式）
     */
    private String handleMarkAsRead(Map<String, String> params) {
        try {
            // 从当前会话获取用户ID
            if (currentUserId == null) { // 如果未登录
                Map<String, Object> result = new HashMap<>(); // 创建结果Map
                result.put("success", false); // 设置成功标志为false
                result.put("message", "用户未登录"); // 设置错误消息
                return generateJsonResponse(result); // 生成JSON响应
            }

            int userId = currentUserId; // 获取当前用户ID
            boolean success = userService.markNotificationsAsRead(userId); // 调用用户服务标记通知为已读

            Map<String, Object> result = new HashMap<>(); // 创建结果Map
            result.put("success", success); // 设置成功标志
            result.put("message", success ? "标记成功" : "标记失败"); // 设置消息
            return generateJsonResponse(result); // 生成JSON响应
        } catch (Exception e) { // 捕获异常
            Map<String, Object> result = new HashMap<>(); // 创建结果Map
            result.put("success", false); // 设置成功标志为false
            result.put("message", e.getMessage()); // 设置异常消息
            return generateJsonResponse(result); // 生成JSON响应
        }
    }

    /**
     * 处理充值
     * @param params 参数Map
     * @return String 响应内容（JSON格式）
     */
    private String handleRecharge(Map<String, String> params) {
        try {
            // 从当前会话获取用户ID
            if (currentUserId == null) { // 如果未登录
                return generateRedirectToLogin(); // 生成重定向到登录页面的响应
            }

            int userId = currentUserId; // 获取当前用户ID
            double amount = Double.parseDouble(params.get("amount")); // 获取充值金额参数并转换为double

            boolean success = userService.recharge(userId, amount); // 调用用户服务充值
            Map<String, Object> result = new HashMap<>(); // 创建结果Map

            if (success) { // 如果充值成功
                result.put("success", true); // 设置成功标志为true
                result.put("message", "充值成功！"); // 设置成功消息
            } else { // 如果充值失败
                result.put("success", false); // 设置成功标志为false
                result.put("message", "充值失败"); // 设置错误消息
            }

            return generateJsonResponse(result); // 生成JSON响应
        } catch (Exception e) { // 捕获异常
            Map<String, Object> result = new HashMap<>(); // 创建结果Map
            result.put("success", false); // 设置成功标志为false
            result.put("message", e.getMessage()); // 设置异常消息
            return generateJsonResponse(result); // 生成JSON响应
        }
    }

    /**
     * 处理登出
     * @return String 响应内容
     */
    private String handleLogout() {
        System.out.println("用户退出登录: ID=" + currentUserId); // 输出登出日志
        currentUserId = null; // 清空当前用户ID

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
        return html; // 返回包含重定向脚本的HTML
    }

    /**
     * 解析POST参数
     * @param exchange HTTP交换对象
     * @return Map<String, String> 参数Map
     * @throws IOException 输入输出异常
     */
    private Map<String, String> parsePostParams(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<>(); // 创建参数Map

        InputStream is = exchange.getRequestBody(); // 获取请求体输入流
        String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8); // 读取请求体内容并转换为字符串

        if (requestBody.contains("&")) { // 如果请求体包含&符号（表示有多个参数）
            String[] pairs = requestBody.split("&"); // 按&分割参数
            for (String pair : pairs) { // 遍历每个参数对
                String[] keyValue = pair.split("="); // 按=分割键值
                if (keyValue.length == 2) { // 如果分割后长度为2（有效的键值对）
                    String key = URLDecoder.decode(keyValue[0], "UTF-8"); // 解码键
                    String value = URLDecoder.decode(keyValue[1], "UTF-8"); // 解码值
                    params.put(key, value); // 放入参数Map
                }
            }
        }

        return params; // 返回参数Map
    }

    /**
     * 解析GET参数
     * @param exchange HTTP交换对象
     * @return Map<String, String> 参数Map
     */
    private Map<String, String> parseGetParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>(); // 创建参数Map
        String query = exchange.getRequestURI().getQuery(); // 获取查询字符串

        if (query != null && !query.isEmpty()) { // 如果查询字符串不为空
            String[] pairs = query.split("&"); // 按&分割参数
            for (String pair : pairs) { // 遍历每个参数对
                String[] keyValue = pair.split("="); // 按=分割键值
                if (keyValue.length == 2) { // 如果分割后长度为2（有效的键值对）
                    try {
                        String key = URLDecoder.decode(keyValue[0], "UTF-8"); // 解码键
                        String value = URLDecoder.decode(keyValue[1], "UTF-8"); // 解码值
                        params.put(key, value); // 放入参数Map
                    } catch (Exception e) { // 捕获解码异常
                        // 忽略解码错误
                    }
                }
            }
        }

        return params; // 返回参数Map
    }

    /**
     * 生成JSON响应
     * @param data 要转换为JSON的数据Map
     * @return String JSON格式的字符串
     */
    private String generateJsonResponse(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{"); // 创建字符串构建器，以{开头
        boolean first = true; // 是否是第一个键值对

        for (Map.Entry<String, Object> entry : data.entrySet()) { // 遍历数据Map
            if (!first) json.append(","); // 如果不是第一个键值对，添加逗号分隔
            json.append("\"").append(entry.getKey()).append("\":"); // 添加键，用双引号包围

            Object value = entry.getValue(); // 获取值
            if (value instanceof String) { // 如果值是字符串类型
                json.append("\"").append(escapeJson((String) value)).append("\""); // 添加转义后的字符串值，用双引号包围
            } else if (value instanceof Number || value instanceof Boolean) { // 如果值是数字或布尔类型
                json.append(value); // 直接添加值
            } else { // 其他类型
                json.append("\"").append(value).append("\""); // 添加值的字符串表示，用双引号包围
            }

            first = false; // 设置第一个键值对标志为false
        }

        json.append("}"); // 以}结尾
        return json.toString(); // 返回JSON字符串
    }

    /**
     * 转义JSON字符串
     * @param str 要转义的字符串
     * @return String 转义后的字符串
     */
    private String escapeJson(String str) {
        if (str == null) return ""; // 如果字符串为null，返回空字符串
        return str.replace("\\", "\\\\") // 转义反斜杠
                .replace("\"", "\\\"") // 转义双引号
                .replace("\n", "\\n") // 转义换行符
                .replace("\r", "\\r") // 转义回车符
                .replace("\t", "\\t"); // 转义制表符
    }

    /**
     * 生成错误页面
     * @param message 错误消息
     * @return String 错误页面的HTML字符串
     */
    private String generateErrorPage(String message) {
        return pageGenerator.generateErrorPage(message); // 调用页面生成器生成错误页面
    }

    /**
     * 生成重定向到登录页面的响应
     * @return String 包含重定向脚本的HTML字符串
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
        return html; // 返回包含重定向脚本的HTML
    }
}