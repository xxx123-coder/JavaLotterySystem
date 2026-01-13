package lottery.ui;

import lottery.model.User;
import lottery.model.LotteryResult;
import java.util.List;
import java.text.SimpleDateFormat;

/**
 * HTML页面生成器
 * 负责生成彩票系统的各个页面
 */
public class PageGenerator {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成登录页面
     */
    public String generateLoginPage(String errorMessage) {
        String html = generateHeader("彩票系统 - 登录");
        html += "<div style='padding: 20px; max-width: 400px; margin: 0 auto;'>";
        html += "<h1>彩票系统登录</h1>";

        if (errorMessage != null) {
            html += "<div style='color: red; padding: 10px; background-color: #ffe6e6; border: 1px solid red;'>";
            html += errorMessage;
            html += "</div><br>";
        }

        html += "<form method='POST' action='login'>";
        html += "用户名: <input type='text' name='username' required><br><br>";
        html += "密码: <input type='password' name='password' required><br><br>";
        html += "<button type='submit'>登录</button>";
        html += "</form>";
        html += "<br>";
        html += "<p>还没有账号？<a href='register'>立即注册</a></p>";
        html += "</div>";
        html += generateFooter();

        return html;
    }

    /**
     * 生成注册页面
     */
    public String generateRegisterPage(String errorMessage) {
        String html = generateHeader("彩票系统 - 注册");
        html += "<div style='padding: 20px; max-width: 400px; margin: 0 auto;'>";
        html += "<h1>用户注册</h1>";

        if (errorMessage != null) {
            html += "<div style='color: red; padding: 10px; background-color: #ffe6e6; border: 1px solid red;'>";
            html += errorMessage;
            html += "</div><br>";
        }

        html += "<form method='POST' action='register'>";
        html += "用户名: <input type='text' name='username' required><br><br>";
        html += "密码: <input type='password' name='password' required><br><br>";
        html += "手机号: <input type='text' name='phone' required><br><br>";
        html += "<button type='submit'>注册</button>";
        html += "</form>";
        html += "<br>";
        html += "<p>已有账号？<a href='login'>立即登录</a></p>";
        html += "</div>";
        html += generateFooter();

        return html;
    }

    /**
     * 生成主页面
     */
    public String generateMainPage(User user) {
        String html = generateHeader("彩票系统 - 主页");
        html += generateNavigation(user);
        html += "<div style='padding: 20px;'>";
        html += "<h1>欢迎, " + user.getUsername() + "!</h1>";
        html += "<div style='background-color: #f0f8ff; padding: 20px; border-radius: 10px;'>";
        html += "<h2>用户信息</h2>";
        html += "<p><strong>用户名:</strong> " + user.getUsername() + "</p>";
        html += "<p><strong>手机号:</strong> " + user.getPhone() + "</p>";
        html += "<p><strong>账户余额:</strong> <span style='color: green; font-weight: bold;'>¥" + user.getBalance() + "</span></p>";
        html += "<button onclick=\"location.href='recharge'\">充值</button>";
        html += "</div>";

        html += "<div style='margin-top: 30px;'>";
        html += "<h2>快速操作</h2>";
        html += "<div style='display: flex; gap: 20px; flex-wrap: wrap;'>";
        html += "<div style='background-color: #e6f7ff; padding: 20px; border-radius: 10px; width: 200px;'>";
        html += "<h3>购买彩票</h3>";
        html += "<p>试试你的运气！</p>";
        html += "<button onclick=\"location.href='buy-ticket'\">立即购买</button>";
        html += "</div>";

        html += "<div style='background-color: #f0ffe6; padding: 20px; border-radius: 10px; width: 200px;'>";
        html += "<h3>我的彩票</h3>";
        html += "<p>查看已购买的彩票</p>";
        html += "<button onclick=\"location.href='my-tickets'\">查看</button>";
        html += "</div>";

        html += "<div style='background-color: #fff0e6; padding: 20px; border-radius: 10px; width: 200px;'>";
        html += "<h3>开奖通知</h3>";
        html += "<p>查看中奖结果</p>";
        html += "<button onclick=\"location.href='notification'\">查看</button>";
        html += "</div>";

        html += "<div style='background-color: #f0e6ff; padding: 20px; border-radius: 10px; width: 200px;'>";
        html += "<h3>抽奖</h3>";
        html += "<p>执行新一期抽奖</p>";
        html += "<button onclick=\"location.href='draw'\">开始抽奖</button>";
        html += "</div>";
        html += "</div>";
        html += "</div>";

        html += "</div>";
        html += generateFooter();

        return html;
    }

    /**
     * 生成购票页面
     */
    public String generateBuyTicketPage() {
        String html = generateHeader("彩票系统 - 购买彩票");
        html += "<div style='padding: 20px; max-width: 600px; margin: 0 auto;'>";
        html += "<h1>购买彩票</h1>";
        html += "<p>每注价格: ¥2.00</p>";

        html += "<div id='manualSection' style='display: none;'>";
        html += "<h3>手动选号</h3>";
        html += "<p>请输入7个1-36之间的数字，用逗号分隔</p>";
        html += "<p>示例: 1,5,12,23,28,31,36</p>";
        html += "<input type='text' id='manualNumbers' placeholder='1,2,3,4,5,6,7' style='width: 300px;'><br><br>";
        html += "</div>";

        html += "<div id='randomSection' style='display: none;'>";
        html += "<h3>随机选号</h3>";
        html += "<p>系统将自动生成7个随机不重复号码</p>";
        html += "</div>";

        html += "<form id='buyForm' method='POST' action='buy-ticket'>";
        html += "选号方式: ";
        html += "<select id='ticketType' onchange='toggleTicketType()'>";
        html += "<option value='manual'>手动选号</option>";
        html += "<option value='random'>随机选号</option>";
        html += "</select><br><br>";

        html += "注数: <input type='number' name='betCount' id='betCount' value='1' min='1'><br><br>";
        html += "<input type='hidden' name='numbers' id='numbers'>";
        html += "<input type='hidden' name='ticketType' id='ticketTypeInput'>";

        html += "<div id='costDisplay' style='margin: 10px 0; padding: 10px; background-color: #f0f0f0;'>";
        html += "总价: ¥<span id='totalCost'>2.00</span>";
        html += "</div>";

        html += "<button type='button' onclick='generateRandomNumbers()' id='generateBtn' style='display: none;'>生成随机号码</button><br><br>";
        html += "<button type='button' onclick='submitForm()'>购买</button>";
        html += "</form>";

        // JavaScript代码
        html += "<script>";
        html += "function toggleTicketType() {";
        html += "  var type = document.getElementById('ticketType').value;";
        html += "  document.getElementById('ticketTypeInput').value = type;";
        html += "  if (type === 'manual') {";
        html += "    document.getElementById('manualSection').style.display = 'block';";
        html += "    document.getElementById('randomSection').style.display = 'none';";
        html += "    document.getElementById('generateBtn').style.display = 'none';";
        html += "  } else {";
        html += "    document.getElementById('manualSection').style.display = 'none';";
        html += "    document.getElementById('randomSection').style.display = 'block';";
        html += "    document.getElementById('generateBtn').style.display = 'inline';";
        html += "    generateRandomNumbers();";
        html += "  }";
        html += "}";

        html += "function generateRandomNumbers() {";
        html += "  var numbers = [];";
        html += "  while (numbers.length < 7) {";
        html += "    var num = Math.floor(Math.random() * 36) + 1;";
        html += "    if (numbers.indexOf(num) === -1) {";
        html += "      numbers.push(num);";
        html += "    }";
        html += "  }";
        html += "  document.getElementById('numbers').value = numbers.join(',');";
        html += "  document.getElementById('manualNumbers').value = numbers.join(',');";
        html += "  document.getElementById('randomSection').innerHTML = '<h3>随机选号</h3><p>生成的号码: ' + numbers.join(', ') + '</p>';";
        html += "}";

        html += "function updateCost() {";
        html += "  var betCount = parseInt(document.getElementById('betCount').value);";
        html += "  var totalCost = betCount * 2;";
        html += "  document.getElementById('totalCost').textContent = totalCost.toFixed(2);";
        html += "}";

        html += "function submitForm() {";
        html += "  var type = document.getElementById('ticketType').value;";
        html += "  if (type === 'manual') {";
        html += "    var numbers = document.getElementById('manualNumbers').value.trim();";
        html += "    if (!numbers) {";
        html += "      alert('请输入号码');";
        html += "      return;";
        html += "    }";
        html += "    document.getElementById('numbers').value = numbers;";
        html += "  }";
        html += "  document.getElementById('buyForm').submit();";
        html += "}";

        html += "document.getElementById('betCount').addEventListener('input', updateCost);";
        html += "toggleTicketType();"; // 初始化
        html += "</script>";

        html += "<br><br><a href='main'>返回主页</a>";
        html += "</div>";
        html += generateFooter();

        return html;
    }

    /**
     * 生成抽奖页面
     */
    public String generateDrawPage() {
        String html = generateHeader("彩票系统 - 抽奖");
        html += "<div style='padding: 20px; text-align: center;'>";
        html += "<h1>彩票抽奖</h1>";
        html += "<div id='resultArea' style='margin: 20px; padding: 20px; background-color: #f0f0f0; border-radius: 10px;'>";
        html += "<p>点击按钮开始新一期抽奖</p>";
        html += "</div>";

        html += "<button onclick='startDraw()' style='padding: 15px 30px; font-size: 18px;'>开始抽奖</button>";

        html += "<div id='loading' style='display: none; margin: 20px;'>";
        html += "<p>正在抽奖中...</p>";
        html += "<div style='width: 100%; background-color: #e0e0e0; border-radius: 10px;'>";
        html += "<div id='progressBar' style='width: 0%; height: 20px; background-color: #4CAF50; border-radius: 10px; transition: width 2s;'></div>";
        html += "</div>";
        html += "</div>";

        // JavaScript代码
        html += "<script>";
        html += "function startDraw() {";
        html += "  document.getElementById('loading').style.display = 'block';";
        html += "  document.getElementById('progressBar').style.width = '100%';";

        html += "  // 发送抽奖请求";
        html += "  var xhr = new XMLHttpRequest();";
        html += "  xhr.open('POST', 'draw', true);";
        html += "  xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');";
        html += "  xhr.onreadystatechange = function() {";
        html += "    if (xhr.readyState === 4 && xhr.status === 200) {";
        html += "      var response = JSON.parse(xhr.responseText);";
        html += "      if (response.success) {";
        html += "        document.getElementById('resultArea').innerHTML = ";
        html += "          '<h2>抽奖完成！</h2>' +";
        html += "          '<h3 style=\"color: red;\">中奖号码: ' + response.winningNumbers + '</h3>' +";
        html += "          '<p>' + (response.result ? '最高奖项: ' + response.result : '本期无人中大奖') + '</p>';";
        html += "      } else {";
        html += "        document.getElementById('resultArea').innerHTML = ";
        html += "          '<h3 style=\"color: red;\">抽奖失败: ' + response.message + '</h3>';";
        html += "      }";
        html += "      document.getElementById('loading').style.display = 'none';";
        html += "      document.getElementById('progressBar').style.width = '0%';";
        html += "    }";
        html += "  };";
        html += "  xhr.send();";
        html += "}";
        html += "</script>";

        html += "<br><br><a href='main'>返回主页</a>";
        html += "</div>";
        html += generateFooter();

        return html;
    }

    /**
     * 生成结果页面
     */
    public String generateResultPage(LotteryResult result) {
        String html = generateHeader("彩票系统 - 中奖结果");
        html += "<div style='padding: 20px; text-align: center;'>";
        html += "<h1>中奖结果</h1>";

        if (result != null) {
            html += "<div style='background-color: #fffacd; padding: 20px; border-radius: 10px; margin: 20px auto; max-width: 500px;'>";
            html += "<h2 style='color: #ff6600;'>恭喜中奖！</h2>";
            html += "<p><strong>中奖号码:</strong> " + result.getWinningNumbers() + "</p>";
            html += "<p><strong>中奖等级:</strong> " + result.getPrizeLevel() + "</p>";
            html += "<p><strong>中奖倍数:</strong> " + result.getMultiplier() + "</p>";
            html += "<p><strong>开奖时间:</strong> " + dateFormat.format(result.getDrawTime()) + "</p>";
            html += "</div>";
        } else {
            html += "<p>暂无中奖结果</p>";
        }

        html += "<br><a href='main'>返回主页</a>";
        html += "</div>";
        html += generateFooter();

        return html;
    }

    /**
     * 生成通知页面
     */
    public String generateNotificationPage(List<LotteryResult> results) {
        String html = generateHeader("彩票系统 - 中奖通知");
        html += "<div style='padding: 20px;'>";
        html += "<h1>中奖通知</h1>";

        if (results == null || results.isEmpty()) {
            html += "<p>您还没有中奖记录，继续努力！</p>";
        } else {
            html += "<p>您有以下中奖记录：</p>";
            html += "<table border='1' style='width: 100%; border-collapse: collapse;'>";
            html += "<tr style='background-color: #f2f2f2;'>";
            html += "<th>中奖号码</th><th>中奖等级</th><th>中奖倍数</th><th>开奖时间</th>";
            html += "</tr>";

            for (LotteryResult result : results) {
                html += "<tr>";
                html += "<td>" + result.getWinningNumbers() + "</td>";
                html += "<td><span style='color: red; font-weight: bold;'>" + result.getPrizeLevel() + "</span></td>";
                html += "<td>" + result.getMultiplier() + "</td>";
                html += "<td>" + dateFormat.format(result.getDrawTime()) + "</td>";
                html += "</tr>";
            }

            html += "</table>";
        }

        html += "<br><br><a href='main'>返回主页</a>";
        html += "</div>";
        html += generateFooter();

        return html;
    }

    /**
     * 生成页面头部
     */
    public String generateHeader(String title) {
        String html = "<!DOCTYPE html>";
        html += "<html>";
        html += "<head>";
        html += "<meta charset='UTF-8'>";
        html += "<title>" + title + "</title>";
        html += "<style>";
        html += "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }";
        html += "header { background-color: #333; color: white; padding: 15px; }";
        html += "h1 { color: #333; }";
        html += "button { padding: 10px 15px; background-color: #4CAF50; color: white; border: none; border-radius: 5px; cursor: pointer; }";
        html += "button:hover { background-color: #45a049; }";
        html += "input, select { padding: 8px; margin: 5px 0; border: 1px solid #ccc; border-radius: 4px; }";
        html += "table { border-collapse: collapse; width: 100%; }";
        html += "th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }";
        html += "th { background-color: #f2f2f2; }";
        html += "tr:nth-child(even) { background-color: #f9f9f9; }";
        html += "</style>";
        html += "</head>";
        html += "<body>";
        html += "<header>";
        html += "<h1 style='color: white; margin: 0;'>彩票系统</h1>";
        html += "</header>";
        return html;
    }

    /**
     * 生成导航栏
     */
    public String generateNavigation(User user) {
        String html = "<nav style='background-color: #444; padding: 10px;'>";
        html += "<a href='main' style='color: white; margin-right: 15px; text-decoration: none;'>主页</a>";
        html += "<a href='buy-ticket' style='color: white; margin-right: 15px; text-decoration: none;'>购买彩票</a>";
        html += "<a href='my-tickets' style='color: white; margin-right: 15px; text-decoration: none;'>我的彩票</a>";
        html += "<a href='notification' style='color: white; margin-right: 15px; text-decoration: none;'>中奖通知</a>";
        html += "<a href='draw' style='color: white; margin-right: 15px; text-decoration: none;'>抽奖</a>";
        html += "<span style='float: right; color: white;'>";
        html += "欢迎, " + user.getUsername() + " | ";
        html += "<a href='logout' style='color: white; text-decoration: none;'>退出</a>";
        html += "</span>";
        html += "</nav>";
        return html;
    }

    /**
     * 生成页面尾部
     */
    public String generateFooter() {
        String html = "<footer style='background-color: #333; color: white; padding: 10px; text-align: center; margin-top: 30px;'>";
        html += "<p>© 2023 彩票系统 - 版权所有</p>";
        html += "</footer>";
        html += "</body>";
        html += "</html>";
        return html;
    }
}