package lottery.ui;

import lottery.service.UserService;
import lottery.service.TicketService;
import lottery.service.LotteryService;
import lottery.model.User;
import lottery.model.LotteryResult;
import lottery.model.Ticket;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求处理Servlet
 * 处理彩票系统的所有HTTP请求
 */
public class ServletHandler extends HttpServlet {
    private final UserService userService;
    private final TicketService ticketService;
    private final LotteryService lotteryService;
    private final PageGenerator pageGenerator;

    public ServletHandler() {
        this.userService = new UserService();
        this.ticketService = new TicketService();
        this.lotteryService = new LotteryService();
        this.pageGenerator = new PageGenerator();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleRequest(request, response);
    }

    /**
     * 分发HTTP请求到相应的处理方法
     */
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativePath = path.substring(contextPath.length());

        // 设置响应编码
        response.setCharacterEncoding("UTF-8");

        // 使用switch语句替代if链
        switch (relativePath) {
            case "/":
            case "/login":
                handleLogin(request, response);
                break;
            case "/register":
                handleRegister(request, response);
                break;
            case "/main":
                handleMain(request, response);
                break;
            case "/buy-ticket":
                handleBuyTicket(request, response);
                break;
            case "/draw":
                handleDraw(request, response);
                break;
            case "/notification":
                handleNotification(request, response);
                break;
            case "/logout":
                handleLogout(request, response);
                break;
            case "/my-tickets":
                handleMyTickets(request, response);
                break;
            case "/recharge":
                handleRecharge(request, response);
                break;
            default:
                // 静态资源或404处理
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendHtmlResponse(response, "<h1>404 - 页面未找到</h1>");
                break;
        }
    }

    /**
     * 处理登录请求
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 提取if语句中的通用部分
        String username = null;
        String password = null;

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            username = request.getParameter("username");
            password = request.getParameter("password");
        }

        if (username != null && password != null) {
            User user = userService.login(username, password);
            if (user != null) {
                // 登录成功，创建会话
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());

                // 重定向到主页面
                response.sendRedirect("main");
                return;
            } else {
                // 登录失败
                sendHtmlResponse(response, pageGenerator.generateLoginPage("用户名或密码错误"));
                return;
            }
        }

        // 显示登录页面
        sendHtmlResponse(response, pageGenerator.generateLoginPage(null));
    }

    /**
     * 处理注册请求
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String phone = request.getParameter("phone");

            if (username != null && password != null && phone != null) {
                try {
                    boolean success = userService.register(username, password, phone);
                    if (success) {
                        sendHtmlResponse(response,
                                "<html><body><h1>注册成功！</h1>" +
                                        "<a href='login'>点击登录</a></body></html>");
                    } else {
                        sendHtmlResponse(response,
                                pageGenerator.generateRegisterPage("用户名已存在"));
                    }
                } catch (IllegalArgumentException e) {
                    sendHtmlResponse(response,
                            pageGenerator.generateRegisterPage(e.getMessage()));
                }
                return;
            }
        }

        // 显示注册页面
        sendHtmlResponse(response, pageGenerator.generateRegisterPage(null));
    }

    /**
     * 处理主页面请求
     */
    private void handleMain(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        // 更新用户信息（获取最新余额）
        user = userService.getUserInfo(user.getId());
        session.setAttribute("user", user);

        sendHtmlResponse(response, pageGenerator.generateMainPage(user));
    }

    /**
     * 处理购买彩票请求
     */
    private void handleBuyTicket(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String ticketType = request.getParameter("ticketType");
            String numbers = request.getParameter("numbers");
            String betCountStr = request.getParameter("betCount");

            try {
                int betCount = Integer.parseInt(betCountStr);

                if ("manual".equals(ticketType) && numbers != null && !numbers.trim().isEmpty()) {
                    // 手动选号购买
                    Ticket ticket = ticketService.buyManualTicket(userId, numbers, betCount);
                    sendJsonResponse(response,
                            createMap("success", true,
                                    "message", "购买成功！彩票号码：" + ticket.getNumbers()));
                } else if ("random".equals(ticketType)) {
                    // 随机选号购买
                    Ticket ticket = ticketService.buyRandomTicket(userId, betCount);
                    sendJsonResponse(response,
                            createMap("success", true,
                                    "message", "购买成功！随机号码：" + ticket.getNumbers()));
                } else {
                    sendJsonResponse(response,
                            createMap("success", false, "message", "参数错误"));
                }
            } catch (Exception e) {
                // 使用日志而不是printStackTrace
                System.err.println("购买彩票失败: " + e.getMessage());
                sendJsonResponse(response,
                        createMap("success", false, "message", "购买失败：" + e.getMessage()));
            }
            return;
        }

        // 显示购票页面
        sendHtmlResponse(response, pageGenerator.generateBuyTicketPage());
    }

    /**
     * 处理抽奖请求
     */
    private void handleDraw(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 检查用户是否已登录（只有管理员才能抽奖，这里简化处理）
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login");
            return;
        }

        // 添加日志输出
        System.out.println("[ServletHandler] 收到抽奖请求，方法: " + request.getMethod());

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            try {
                System.out.println("[ServletHandler] 开始执行抽奖...");

                // 执行抽奖
                String winningNumbers = lotteryService.drawLottery();
                System.out.println("[ServletHandler] 抽奖完成，中奖号码: " + winningNumbers);

                // 获取最新开奖结果
                List<LotteryResult> allResults = lotteryService.getAllLotteryResults(); // 新增方法
                LotteryResult latestResult = allResults.isEmpty() ? null : allResults.get(allResults.size() - 1);

                System.out.println("[ServletHandler] 最新开奖结果: " + (latestResult != null ?
                        "等级=" + latestResult.getPrizeLevel() + ", 号码=" + latestResult.getWinningNumbers() : "无"));

                // 构建详细响应
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("winningNumbers", winningNumbers);
                responseData.put("message", "抽奖完成！本期中奖号码已生成");

                if (latestResult != null) {
                    responseData.put("prizeLevel", latestResult.getPrizeLevel());
                    responseData.put("drawTime", latestResult.getDrawTime());

                    // 获取中奖人数统计
                    int winnerCount = lotteryService.getWinningTicketCount(winningNumbers);
                    responseData.put("winnerCount", winnerCount);

                    if (winnerCount > 0) {
                        responseData.put("winnerInfo", "本期共有 " + winnerCount + " 张彩票中奖");
                    } else {
                        responseData.put("winnerInfo", "本期无人中奖");
                    }
                }

                System.out.println("[ServletHandler] 发送JSON响应: " + responseData);
                sendJsonResponse(response, responseData);

            } catch (Exception e) {
                // 使用日志而不是printStackTrace
                System.err.println("[ServletHandler] 抽奖失败: " + e.getMessage());
                sendJsonResponse(response,
                        createMap("success", false, "message", "抽奖失败：" + e.getMessage()));
            }
            return;
        }

        // 显示抽奖页面
        sendHtmlResponse(response, pageGenerator.generateDrawPage());
    }

    /**
     * 处理通知请求
     */
    private void handleNotification(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // 获取用户的中奖记录
        List<LotteryResult> results = lotteryService.getUserWinningResults(userId);

        sendHtmlResponse(response, pageGenerator.generateNotificationPage(results));
    }

    /**
     * 处理我的彩票请求
     */
    private void handleMyTickets(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // 获取用户的彩票
        List<Ticket> tickets = ticketService.getUserTickets(userId);

        // 生成彩票列表页面，使用StringBuilder优化字符串拼接
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(pageGenerator.generateHeader("我的彩票"));
        htmlBuilder.append("<div style='padding: 20px;'>");
        htmlBuilder.append("<h1>我的彩票</h1>");

        if (tickets.isEmpty()) {
            htmlBuilder.append("<p>您还没有购买彩票。</p>");
        } else {
            htmlBuilder.append("<table border='1' style='width: 100%; border-collapse: collapse;'>");
            htmlBuilder.append("<tr><th>彩票ID</th><th>号码</th><th>注数</th><th>购买时间</th><th>选号方式</th></tr>");

            for (Ticket ticket : tickets) {
                htmlBuilder.append("<tr>");
                htmlBuilder.append("<td>").append(ticket.getId()).append("</td>");
                htmlBuilder.append("<td>").append(ticket.getNumbers()).append("</td>");
                htmlBuilder.append("<td>").append(ticket.getBetCount()).append("</td>");
                htmlBuilder.append("<td>").append(ticket.getPurchaseTime()).append("</td>");
                htmlBuilder.append("<td>").append(ticket.isManual() ? "手动选号" : "随机选号").append("</td>");
                htmlBuilder.append("</tr>");
            }

            htmlBuilder.append("</table>");
        }

        htmlBuilder.append("<br><a href='main'>返回主页</a>");
        htmlBuilder.append("</div>");
        htmlBuilder.append(pageGenerator.generateFooter());

        sendHtmlResponse(response, htmlBuilder.toString());
    }

    /**
     * 处理充值请求
     */
    private void handleRecharge(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String amountStr = request.getParameter("amount");

            try {
                double amount = Double.parseDouble(amountStr);
                boolean success = userService.recharge(userId, amount);

                if (success) {
                    // 更新会话中的用户信息
                    User user = userService.getUserInfo(userId);
                    session.setAttribute("user", user);

                    sendJsonResponse(response,
                            createMap("success", true,
                                    "message", "充值成功！当前余额：" + user.getBalance()));
                } else {
                    sendJsonResponse(response,
                            createMap("success", false, "message", "充值失败"));
                }
            } catch (Exception e) {
                sendJsonResponse(response,
                        createMap("success", false, "message", "充值失败：" + e.getMessage()));
            }
            return;
        }

        // 显示充值页面
        String html = pageGenerator.generateHeader("账户充值");
        html += "<div style='padding: 20px;'>";
        html += "<h1>账户充值</h1>";
        html += "<form method='POST'>";
        html += "充值金额: <input type='number' name='amount' min='1' step='0.01' required><br><br>";
        html += "<button type='submit'>确认充值</button>";
        html += "</form>";
        html += "<br><a href='main'>返回主页</a>";
        html += "</div>";
        html += pageGenerator.generateFooter();

        sendHtmlResponse(response, html);
    }

    /**
     * 处理登出请求
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("login");
    }

    /**
     * 发送JSON格式响应
     */
    private void sendJsonResponse(HttpServletResponse response, Map<String, Object> data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 改进的JSON序列化，正确处理不同类型
        StringBuilder json = new StringBuilder("{");
        if (data != null) {
            boolean first = true;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(entry.getKey()).append("\":");

                Object value = entry.getValue();
                if (value instanceof String) {
                    json.append("\"").append(escapeJsonString(value.toString())).append("\"");
                } else if (value instanceof Number) {
                    json.append(value);
                } else if (value instanceof Boolean) {
                    json.append(value);
                } else if (value == null) {
                    json.append("null");
                } else {
                    // 其他类型转换为字符串，移除不必要的toString()
                    json.append("\"").append(escapeJsonString(String.valueOf(value))).append("\"");
                }
                first = false;
            }
        }
        json.append("}");

        System.out.println("[ServletHandler] 发送JSON: " + json.toString());
        out.print(json.toString());
        out.flush();
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 发送HTML格式响应
     */
    private void sendHtmlResponse(HttpServletResponse response, String html) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(html);
        out.flush();
    }

    /**
     * 辅助方法：创建Map的简写
     */
    private Map<String, Object> createMap(Object... keyValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i + 1 < keyValues.length) {
                map.put(keyValues[i].toString(), keyValues[i + 1]);
            }
        }
        return map;
    }
}