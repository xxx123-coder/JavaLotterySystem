package lottery.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 页面生成器
 */
public class PageGenerator {

    /**
     * 生成登录页面
     */
    public String generateLoginPage(String errorMessage) {
        String html = generateHeader("登录");
        html += "<div style='max-width: 300px; margin: 50px auto; padding: 20px; border: 1px solid #ccc;'>";
        html += "<h2>用户登录</h2>";

        if (errorMessage != null) {
            html += "<div style='color: red;'>" + errorMessage + "</div>";
        }

        html += "<form method='POST' action='/login'>";
        html += "用户名: <input type='text' name='username' required><br><br>";
        html += "密码: <input type='password' name='password' required><br><br>";
        html += "<button type='submit'>登录</button>";
        html += "<a href='/register' style='margin-left: 20px;'>注册</a>";
        html += "</form>";
        html += "</div>";
        html += generateFooter();
        return html;
    }

    /**
     * 生成注册页面
     */
    public String generateRegisterPage(String errorMessage) {
        String html = generateHeader("注册");
        html += "<div style='max-width: 300px; margin: 50px auto; padding: 20px; border: 1px solid #ccc;'>";
        html += "<h2>用户注册</h2>";

        if (errorMessage != null) {
            html += "<div style='color: red;'>" + errorMessage + "</div>";
        }

        html += "<form method='POST' action='/register'>";
        html += "用户名: <input type='text' name='username' required><br><br>";
        html += "密码: <input type='password' name='password' required><br><br>";
        html += "电话: <input type='text' name='phone' required><br><br>";
        html += "<button type='submit'>注册</button>";
        html += "<a href='/login' style='margin-left: 20px;'>返回登录</a>";
        html += "</form>";
        html += "</div>";
        html += generateFooter();
        return html;
    }

    /**
     * 生成主页面
     */
    public String generateMainPage(Map<String, Object> user) {
        String html = generateHeader("彩票系统");
        html += "<div style='padding: 20px;'>";
        html += "<h1>欢迎使用彩票系统</h1>";

        if (user != null) {
            html += "<p>用户: " + user.get("username") + "</p>";
            html += "<p>余额: ￥" + user.get("balance") + "</p>";

            // 新增：显示未读中奖通知
            Object unreadCountObj = user.get("unreadWinningCount");
            if (unreadCountObj != null) {
                int unreadCount = ((Number) unreadCountObj).intValue();
                if (unreadCount > 0) {
                    html += "<div style='background-color: #ffeb3b; padding: 10px; margin: 10px 0; border: 1px solid #ffc107;'>";
                    html += "<strong>🎉 中奖通知：</strong>您有 " + unreadCount + " 条未读中奖记录！";
                    html += " <a href='/check-winning'>点击查看</a>";
                    html += "</div>";
                }
            }
        }

        html += "<hr>";
        html += "<h3>功能菜单</h3>";
        html += "<ul>";
        html += "<li><a href='/buy-ticket'>购买彩票</a></li>";
        html += "<li><a href='/draw'>开始抽奖</a></li>";
        html += "<li><a href='/my-tickets'>我的彩票</a></li>";
        html += "<li><a href='/check-winning'>中奖查询</a></li>"; // 新增
        html += "<li><a href='/recharge'>账户充值</a></li>";
        html += "<li><a href='/logout'>退出登录</a></li>";
        html += "</ul>";
        html += "</div>";
        html += generateFooter();
        return html;
    }

    /**
     * 生成购票页面
     */
    public String generateBuyTicketPage(int userId) {
        String html = generateHeader("购买彩票");
        html += "<div style='padding: 20px;'>";
        html += "<h1>购买彩票</h1>";
        html += "<form method='POST' action='/buy-ticket'>";
        html += "<input type='hidden' name='userId' value='" + userId + "'>";

        html += "<div style='margin: 10px 0;'>";
        html += "投注方式: ";
        html += "<input type='radio' name='ticketType' value='manual' checked> 手动选号 ";
        html += "<input type='radio' name='ticketType' value='random'> 随机选号";
        html += "</div>";

        html += "<div style='margin: 10px 0;'>";
        html += "号码 (1-36, 7个逗号分隔): <input type='text' name='numbers'><br>";
        html += "<small>示例: 1,2,3,4,5,6,7</small>";
        html += "</div>";

        html += "<div style='margin: 10px 0;'>";
        html += "注数: <input type='number' name='betCount' value='1' min='1'>";
        html += "</div>";

        html += "<button type='submit'>购买 (每注￥2)</button>";
        html += "</form>";
        html += "<br><a href='/main'>返回主页</a>";
        html += "</div>";
        html += generateFooter();
        return html;
    }

    /**
     * 生成抽奖页面
     */
    public String generateDrawPage() {
        String html = generateHeader("抽奖");
        html += "<div style='padding: 20px; text-align: center;'>";
        html += "<h1>彩票抽奖</h1>";
        html += "<div id='result' style='font-size: 24px; margin: 20px 0;'></div>";
        html += "<div id='summary' style='font-size: 16px; margin: 10px 0; color: #666;'></div>";
        html += "<button onclick='draw()' style='padding: 10px 20px; font-size: 18px;'>开始抽奖</button>";
        html += "<br><br><a href='/main'>返回主页</a>";
        html += "</div>";

        // JavaScript代码
        html += "<script>";
        html += "function draw() {";
        html += "  fetch('/draw', {method: 'POST'})";
        html += "    .then(response => response.json())";
        html += "    .then(data => {";
        html += "      document.getElementById('result').innerHTML = '中奖号码: ' + data.winningNumbers;";
        html += "      document.getElementById('summary').innerHTML = data.message;";
        html += "    });";
        html += "}";
        html += "</script>";

        html += generateFooter();
        return html;
    }

    /**
     * 生成我的彩票页面
     */
    public String generateMyTicketsPage(List<Map<String, Object>> tickets) {
        String html = generateHeader("我的彩票");
        html += "<div style='padding: 20px;'>";
        html += "<h1>我的彩票</h1>";

        if (tickets == null || tickets.isEmpty()) {
            html += "<p>您还没有购买彩票。</p>";
        } else {
            html += "<table border='1' style='width: 100%;'>";
            html += "<tr><th>ID</th><th>号码</th><th>注数</th><th>购买时间</th><th>类型</th></tr>";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Map<String, Object> ticket : tickets) {
                html += "<tr>";
                html += "<td>" + ticket.get("id") + "</td>";
                html += "<td>" + ticket.get("numbers") + "</td>";
                html += "<td>" + ticket.get("betCount") + "</td>";

                Date purchaseTime = (Date) ticket.get("purchaseTime");
                html += "<td>" + (purchaseTime != null ? sdf.format(purchaseTime) : "") + "</td>";

                boolean isManual = (Boolean) ticket.get("manual");
                html += "<td>" + (isManual ? "手动" : "随机") + "</td>";
                html += "</tr>";
            }
            html += "</table>";
        }

        html += "<br><a href='/main'>返回主页</a>";
        html += "</div>";
        html += generateFooter();
        return html;
    }

    /**
     * 生成中奖查询页面（新增）
     */
    public String generateWinningPage(List<Map<String, Object>> winnings, List<Map<String, Object>> unreadWinnings) {
        String html = generateHeader("中奖查询");
        html += "<div style='padding: 20px;'>";
        html += "<h1>中奖查询</h1>";

        // 显示未读中奖通知
        if (unreadWinnings != null && !unreadWinnings.isEmpty()) {
            html += "<div style='background-color: #e8f5e8; padding: 15px; margin: 10px 0; border: 1px solid #4caf50;'>";
            html += "<h3 style='color: #2e7d32;'>🎉 未读中奖通知</h3>";
            html += "<table border='1' style='width: 100%;'>";
            html += "<tr><th>期号</th><th>匹配号码</th><th>中奖等级</th><th>奖金</th><th>中奖时间</th></tr>";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Map<String, Object> winning : unreadWinnings) {
                html += "<tr>";
                html += "<td>" + winning.get("resultId") + "</td>";
                html += "<td>" + winning.get("matchCount") + "个</td>";
                html += "<td>" + winning.get("prizeLevel") + "</td>";
                html += "<td>￥" + winning.get("prizeAmount") + "</td>";

                Date winTime = (Date) winning.get("winTime");
                html += "<td>" + (winTime != null ? sdf.format(winTime) : "") + "</td>";
                html += "</tr>";
            }
            html += "</table>";
            html += "<p><a href='javascript:markAsRead()'>标记为已读</a></p>";
            html += "</div>";
        }

        // 显示历史中奖记录
        html += "<h3>历史中奖记录</h3>";
        if (winnings == null || winnings.isEmpty()) {
            html += "<p>暂无历史中奖记录。</p>";
        } else {
            html += "<table border='1' style='width: 100%;'>";
            html += "<tr><th>期号</th><th>匹配号码</th><th>中奖等级</th><th>奖金</th><th>中奖时间</th><th>通知状态</th></tr>";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Map<String, Object> winning : winnings) {
                html += "<tr>";
                html += "<td>" + winning.get("resultId") + "</td>";
                html += "<td>" + winning.get("matchCount") + "个</td>";
                html += "<td>" + winning.get("prizeLevel") + "</td>";
                html += "<td>￥" + winning.get("prizeAmount") + "</td>";

                Date winTime = (Date) winning.get("winTime");
                html += "<td>" + (winTime != null ? sdf.format(winTime) : "") + "</td>";

                Object isNotifiedObj = winning.get("isNotified");
                boolean isNotified = false;
                if (isNotifiedObj instanceof Boolean) {
                    isNotified = (Boolean) isNotifiedObj;
                } else if (isNotifiedObj instanceof String) {
                    isNotified = Boolean.parseBoolean((String) isNotifiedObj);
                }
                html += "<td>" + (isNotified ? "已读" : "<span style='color: red;'>未读</span>") + "</td>";
                html += "</tr>";
            }
            html += "</table>";
        }

        html += "<br><a href='/main'>返回主页</a>";
        html += "</div>";

        // JavaScript代码
        html += "<script>";
        html += "function markAsRead() {";
        html += "  fetch('/mark-read', {method: 'POST'})";
        html += "    .then(response => response.json())";
        html += "    .then(data => {";
        html += "      if (data.success) {";
        html += "        alert('标记成功！');";
        html += "        location.reload();";
        html += "      } else {";
        html += "        alert('标记失败：' + data.message);";
        html += "      }";
        html += "    });";
        html += "}";
        html += "</script>";

        html += generateFooter();
        return html;
    }

    /**
     * 生成充值页面
     */
    public String generateRechargePage(int userId) {
        String html = generateHeader("账户充值");
        html += "<div style='padding: 20px; max-width: 300px; margin: 0 auto;'>";
        html += "<h1>账户充值</h1>";
        html += "<form method='POST' action='/recharge'>";
        html += "<input type='hidden' name='userId' value='" + userId + "'>";
        html += "充值金额: <input type='number' name='amount' min='1' step='0.01' required><br><br>";
        html += "<button type='submit'>确认充值</button>";
        html += "</form>";
        html += "<br><a href='/main'>返回主页</a>";
        html += "</div>";
        html += generateFooter();
        return html;
    }

    /**
     * 生成成功页面
     */
    public String generateSuccessPage(String message, String redirectPage) {
        String html = generateHeader("操作成功");
        html += "<div style='padding: 20px; text-align: center;'>";
        html += "<h1>成功</h1>";
        html += "<p>" + message + "</p>";
        html += "<br><a href='" + redirectPage + "'>点击继续</a>";
        html += "</div>";
        html += generateFooter();
        return html;
    }

    /**
     * 生成错误页面
     */
    public String generateErrorPage(String message) {
        String html = generateHeader("错误");
        html += "<div style='padding: 20px; text-align: center;'>";
        html += "<h1>错误</h1>";
        html += "<p>" + message + "</p>";
        html += "<br><a href='/main'>返回主页</a>";
        html += "</div>";
        html += generateFooter();
        return html;
    }

    /**
     * 生成页面头部
     */
    private String generateHeader(String title) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>" + title + "</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; margin: 0; padding: 0; }" +
                "header { background: #333; color: white; padding: 10px 20px; }" +
                "a { color: #0066cc; text-decoration: none; }" +
                "a:hover { text-decoration: underline; }" +
                "table { border-collapse: collapse; width: 100%; }" +
                "th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }" +
                "th { background-color: #f2f2f2; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<header><h1>彩票系统</h1></header>";
    }

    /**
     * 生成页面底部
     */
    private String generateFooter() {
        return "<footer style='margin-top: 20px; padding: 10px; background: #eee; text-align: center;'>" +
                "彩票系统 © 2023" +
                "</footer>" +
                "</body>" +
                "</html>";
    }
}