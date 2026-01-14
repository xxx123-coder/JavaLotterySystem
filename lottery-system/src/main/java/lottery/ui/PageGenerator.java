package lottery.ui; // 定义包路径，表明该类属于lottery.ui包，负责页面生成

// 导入必要的Java类和包
import java.text.SimpleDateFormat; // 导入简单日期格式化类，用于格式化日期
import java.util.Date; // 导入日期类，用于处理日期和时间
import java.util.List; // 导入List接口，用于处理列表数据
import java.util.Map; // 导入Map接口，用于处理键值对数据

/**
 * 页面生成器
 * 负责生成所有Web页面的HTML内容
 */
public class PageGenerator {

    /**
     * 生成登录页面
     * @param errorMessage 错误消息，如果为null则不显示错误
     * @return String 登录页面的HTML字符串
     */
    public String generateLoginPage(String errorMessage) {
        String html = generateHeader("登录"); // 生成页面头部，标题为"登录"
        html += "<div style='max-width: 300px; margin: 50px auto; padding: 20px; border: 1px solid #ccc;'>"; // 添加样式化的div容器
        html += "<h2>用户登录</h2>"; // 添加标题

        if (errorMessage != null) { // 如果错误消息不为空
            html += "<div style='color: red;'>" + errorMessage + "</div>"; // 显示错误消息，红色字体
        }

        html += "<form method='POST' action='/login'>"; // 创建POST表单，提交到/login路径
        html += "用户名: <input type='text' name='username' required><br><br>"; // 用户名输入框，必填
        html += "密码: <input type='password' name='password' required><br><br>"; // 密码输入框，必填
        html += "<button type='submit'>登录</button>"; // 提交按钮
        html += "<a href='/register' style='margin-left: 20px;'>注册</a>"; // 注册链接
        html += "</form>"; // 结束表单
        html += "</div>"; // 结束div容器
        html += generateFooter(); // 生成页面底部
        return html; // 返回完整的HTML字符串
    }

    /**
     * 生成注册页面
     * @param errorMessage 错误消息，如果为null则不显示错误
     * @return String 注册页面的HTML字符串
     */
    public String generateRegisterPage(String errorMessage) {
        String html = generateHeader("注册"); // 生成页面头部，标题为"注册"
        html += "<div style='max-width: 300px; margin: 50px auto; padding: 20px; border: 1px solid #ccc;'>"; // 添加样式化的div容器
        html += "<h2>用户注册</h2>"; // 添加标题

        if (errorMessage != null) { // 如果错误消息不为空
            html += "<div style='color: red;'>" + errorMessage + "</div>"; // 显示错误消息，红色字体
        }

        html += "<form method='POST' action='/register'>"; // 创建POST表单，提交到/register路径
        html += "用户名: <input type='text' name='username' required><br><br>"; // 用户名输入框，必填
        html += "密码: <input type='password' name='password' required><br><br>"; // 密码输入框，必填
        html += "电话: <input type='text' name='phone' required><br><br>"; // 电话输入框，必填
        html += "<button type='submit'>注册</button>"; // 提交按钮
        html += "<a href='/login' style='margin-left: 20px;'>返回登录</a>"; // 返回登录链接
        html += "</form>"; // 结束表单
        html += "</div>"; // 结束div容器
        html += generateFooter(); // 生成页面底部
        return html; // 返回完整的HTML字符串
    }

    /**
     * 生成主页面
     * @param user 用户信息Map，如果为null则不显示用户信息
     * @return String 主页面的HTML字符串
     */
    public String generateMainPage(Map<String, Object> user) {
        String html = generateHeader("彩票系统"); // 生成页面头部，标题为"彩票系统"
        html += "<div style='padding: 20px;'>"; // 添加内边距为20px的div容器
        html += "<h1>欢迎使用彩票系统</h1>"; // 添加主标题

        if (user != null) { // 如果用户信息不为空
            html += "<p>用户: " + user.get("username") + "</p>"; // 显示用户名
            html += "<p>余额: ￥" + user.get("balance") + "</p>"; // 显示余额

            // 新增：显示未读中奖通知
            Object unreadCountObj = user.get("unreadWinningCount"); // 获取未读中奖通知数量对象
            if (unreadCountObj != null) { // 如果不为空
                int unreadCount = ((Number) unreadCountObj).intValue(); // 转换为整数
                if (unreadCount > 0) { // 如果未读数量大于0
                    html += "<div style='background-color: #ffeb3b; padding: 10px; margin: 10px 0; border: 1px solid #ffc107;'>"; // 添加样式化的通知div
                    html += "<strong>🎉 中奖通知：</strong>您有 " + unreadCount + " 条未读中奖记录！"; // 显示未读中奖通知
                    html += " <a href='/check-winning'>点击查看</a>"; // 添加查看链接
                    html += "</div>"; // 结束通知div
                }
            }
        }

        html += "<hr>"; // 添加水平线
        html += "<h3>功能菜单</h3>"; // 添加功能菜单标题
        html += "<ul>"; // 开始无序列表
        html += "<li><a href='/buy-ticket'>购买彩票</a></li>"; // 购买彩票菜单项
        html += "<li><a href='/draw'>开始抽奖</a></li>"; // 开始抽奖菜单项
        html += "<li><a href='/my-tickets'>我的彩票</a></li>"; // 我的彩票菜单项
        html += "<li><a href='/check-winning'>中奖查询</a></li>"; // 新增：中奖查询菜单项
        html += "<li><a href='/recharge'>账户充值</a></li>"; // 账户充值菜单项
        html += "<li><a href='/logout'>退出登录</a></li>"; // 退出登录菜单项
        html += "</ul>"; // 结束无序列表
        html += "</div>"; // 结束div容器
        html += generateFooter(); // 生成页面底部
        return html; // 返回完整的HTML字符串
    }

    /**
     * 生成购票页面
     * @param userId 用户ID
     * @return String 购票页面的HTML字符串
     */
    public String generateBuyTicketPage(int userId) {
        String html = generateHeader("购买彩票"); // 生成页面头部，标题为"购买彩票"
        html += "<div style='padding: 20px;'>"; // 添加内边距为20px的div容器
        html += "<h1>购买彩票</h1>"; // 添加主标题
        html += "<form method='POST' action='/buy-ticket'>"; // 创建POST表单，提交到/buy-ticket路径
        html += "<input type='hidden' name='userId' value='" + userId + "'>"; // 隐藏字段，存储用户ID

        html += "<div style='margin: 10px 0;'>"; // 添加外边距的div
        html += "投注方式: "; // 投注方式标签
        html += "<input type='radio' name='ticketType' value='manual' checked> 手动选号 "; // 手动选号单选按钮，默认选中
        html += "<input type='radio' name='ticketType' value='random'> 随机选号"; // 随机选号单选按钮
        html += "</div>"; // 结束div

        html += "<div style='margin: 10px 0;'>"; // 添加外边距的div
        html += "号码 (1-36, 7个逗号分隔): <input type='text' name='numbers'><br>"; // 号码输入框和说明
        html += "<small>示例: 1,2,3,4,5,6,7</small>"; // 示例文本
        html += "</div>"; // 结束div

        html += "<div style='margin: 10px 0;'>"; // 添加外边距的div
        html += "注数: <input type='number' name='betCount' value='1' min='1'>"; // 注数输入框，默认值1，最小值1
        html += "</div>"; // 结束div

        html += "<button type='submit'>购买 (每注￥2)</button>"; // 提交按钮
        html += "</form>"; // 结束表单
        html += "<br><a href='/main'>返回主页</a>"; // 返回主页链接
        html += "</div>"; // 结束div容器
        html += generateFooter(); // 生成页面底部
        return html; // 返回完整的HTML字符串
    }

    /**
     * 生成抽奖页面
     * @return String 抽奖页面的HTML字符串
     */
    public String generateDrawPage() {
        String html = generateHeader("抽奖"); // 生成页面头部，标题为"抽奖"
        html += "<div style='padding: 20px; text-align: center;'>"; // 添加居中样式的div容器
        html += "<h1>彩票抽奖</h1>"; // 添加主标题
        html += "<div id='result' style='font-size: 24px; margin: 20px 0;'></div>"; // 显示结果的div，ID为result
        html += "<div id='summary' style='font-size: 16px; margin: 10px 0; color: #666;'></div>"; // 显示摘要的div，ID为summary
        html += "<button onclick='draw()' style='padding: 10px 20px; font-size: 18px;'>开始抽奖</button>"; // 开始抽奖按钮，点击触发draw()函数
        html += "<br><br><a href='/main'>返回主页</a>"; // 返回主页链接
        html += "</div>"; // 结束div容器

        // JavaScript代码
        html += "<script>"; // 开始JavaScript代码块
        html += "function draw() {"; // 定义draw()函数
        html += "  fetch('/draw', {method: 'POST'})"; // 使用fetch API向/draw路径发送POST请求
        html += "    .then(response => response.json())"; // 将响应解析为JSON
        html += "    .then(data => {"; // 处理解析后的数据
        html += "      document.getElementById('result').innerHTML = '中奖号码: ' + data.winningNumbers;"; // 将中奖号码显示在result div中
        html += "      document.getElementById('summary').innerHTML = data.message;"; // 将消息显示在summary div中
        html += "    });"; // 结束then回调
        html += "}"; // 结束draw()函数
        html += "</script>"; // 结束JavaScript代码块

        html += generateFooter(); // 生成页面底部
        return html; // 返回完整的HTML字符串
    }

    /**
     * 生成我的彩票页面
     * @param tickets 彩票列表，每个元素是一个Map<String, Object>
     * @return String 我的彩票页面的HTML字符串
     */
    public String generateMyTicketsPage(List<Map<String, Object>> tickets) {
        String html = generateHeader("我的彩票"); // 生成页面头部，标题为"我的彩票"
        html += "<div style='padding: 20px;'>"; // 添加内边距为20px的div容器
        html += "<h1>我的彩票</h1>"; // 添加主标题

        if (tickets == null || tickets.isEmpty()) { // 如果彩票列表为空或null
            html += "<p>您还没有购买彩票。</p>"; // 显示提示信息
        } else {
            html += "<table border='1' style='width: 100%;'>"; // 创建表格，边框为1，宽度100%
            html += "<tr><th>ID</th><th>号码</th><th>注数</th><th>购买时间</th><th>类型</th></tr>"; // 表格表头行

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 创建日期格式化对象
            for (Map<String, Object> ticket : tickets) { // 遍历彩票列表
                html += "<tr>"; // 开始表格行
                html += "<td>" + ticket.get("id") + "</td>"; // 显示彩票ID
                html += "<td>" + ticket.get("numbers") + "</td>"; // 显示号码
                html += "<td>" + ticket.get("betCount") + "</td>"; // 显示注数

                Date purchaseTime = (Date) ticket.get("purchaseTime"); // 获取购买时间
                html += "<td>" + (purchaseTime != null ? sdf.format(purchaseTime) : "") + "</td>"; // 格式化显示购买时间，如果为空显示空字符串

                boolean isManual = (Boolean) ticket.get("manual"); // 获取是否手动选号
                html += "<td>" + (isManual ? "手动" : "随机") + "</td>"; // 显示类型
                html += "</tr>"; // 结束表格行
            }
            html += "</table>"; // 结束表格
        }

        html += "<br><a href='/main'>返回主页</a>"; // 返回主页链接
        html += "</div>"; // 结束div容器
        html += generateFooter(); // 生成页面底部
        return html; // 返回完整的HTML字符串
    }

    /**
     * 生成中奖查询页面（新增）
     * @param winnings 所有中奖记录列表
     * @param unreadWinnings 未读中奖记录列表
     * @return String 中奖查询页面的HTML字符串
     */
    public String generateWinningPage(List<Map<String, Object>> winnings, List<Map<String, Object>> unreadWinnings) {
        String html = generateHeader("中奖查询"); // 生成页面头部，标题为"中奖查询"
        html += "<div style='padding: 20px;'>"; // 添加内边距为20px的div容器
        html += "<h1>中奖查询</h1>"; // 添加主标题

        // 显示未读中奖通知
        if (unreadWinnings != null && !unreadWinnings.isEmpty()) { // 如果未读中奖记录不为空
            html += "<div style='background-color: #e8f5e8; padding: 15px; margin: 10px 0; border: 1px solid #4caf50;'>"; // 添加样式化的通知div
            html += "<h3 style='color: #2e7d32;'>🎉 未读中奖通知</h3>"; // 未读中奖通知标题
            html += "<table border='1' style='width: 100%;'>"; // 创建表格
            html += "<tr><th>期号</th><th>匹配号码</th><th>中奖等级</th><th>奖金</th><th>中奖时间</th></tr>"; // 表格表头

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 创建日期格式化对象
            for (Map<String, Object> winning : unreadWinnings) { // 遍历未读中奖记录
                html += "<tr>"; // 开始表格行
                html += "<td>" + winning.get("resultId") + "</td>"; // 显示期号
                html += "<td>" + winning.get("matchCount") + "个</td>"; // 显示匹配号码数量
                html += "<td>" + winning.get("prizeLevel") + "</td>"; // 显示中奖等级
                html += "<td>￥" + winning.get("prizeAmount") + "</td>"; // 显示奖金

                Date winTime = (Date) winning.get("winTime"); // 获取中奖时间
                html += "<td>" + (winTime != null ? sdf.format(winTime) : "") + "</td>"; // 格式化显示中奖时间
                html += "</tr>"; // 结束表格行
            }
            html += "</table>"; // 结束表格
            html += "<p><a href='javascript:markAsRead()'>标记为已读</a></p>"; // 标记为已读链接，点击触发markAsRead()函数
            html += "</div>"; // 结束通知div
        }

        // 显示历史中奖记录
        html += "<h3>历史中奖记录</h3>"; // 历史中奖记录标题
        if (winnings == null || winnings.isEmpty()) { // 如果历史中奖记录为空
            html += "<p>暂无历史中奖记录。</p>"; // 显示提示信息
        } else {
            html += "<table border='1' style='width: 100%;'>"; // 创建表格
            html += "<tr><th>期号</th><th>匹配号码</th><th>中奖等级</th><th>奖金</th><th>中奖时间</th><th>通知状态</th></tr>"; // 表格表头

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 创建日期格式化对象
            for (Map<String, Object> winning : winnings) { // 遍历历史中奖记录
                html += "<tr>"; // 开始表格行
                html += "<td>" + winning.get("resultId") + "</td>"; // 显示期号
                html += "<td>" + winning.get("matchCount") + "个</td>"; // 显示匹配号码数量
                html += "<td>" + winning.get("prizeLevel") + "</td>"; // 显示中奖等级
                html += "<td>￥" + winning.get("prizeAmount") + "</td>"; // 显示奖金

                Date winTime = (Date) winning.get("winTime"); // 获取中奖时间
                html += "<td>" + (winTime != null ? sdf.format(winTime) : "") + "</td>"; // 格式化显示中奖时间

                Object isNotifiedObj = winning.get("isNotified"); // 获取通知状态对象
                boolean isNotified = false; // 通知状态变量
                if (isNotifiedObj instanceof Boolean) { // 如果是Boolean类型
                    isNotified = (Boolean) isNotifiedObj; // 赋值
                } else if (isNotifiedObj instanceof String) { // 如果是String类型
                    isNotified = Boolean.parseBoolean((String) isNotifiedObj); // 转换为boolean
                }
                html += "<td>" + (isNotified ? "已读" : "<span style='color: red;'>未读</span>") + "</td>"; // 显示通知状态，未读为红色
                html += "</tr>"; // 结束表格行
            }
            html += "</table>"; // 结束表格
        }

        html += "<br><a href='/main'>返回主页</a>"; // 返回主页链接
        html += "</div>"; // 结束div容器

        // JavaScript代码
        html += "<script>"; // 开始JavaScript代码块
        html += "function markAsRead() {"; // 定义markAsRead()函数
        html += "  fetch('/mark-read', {method: 'POST'})"; // 向/mark-read路径发送POST请求
        html += "    .then(response => response.json())"; // 将响应解析为JSON
        html += "    .then(data => {"; // 处理解析后的数据
        html += "      if (data.success) {"; // 如果操作成功
        html += "        alert('标记成功！');"; // 显示成功提示
        html += "        location.reload();"; // 刷新页面
        html += "      } else {"; // 如果操作失败
        html += "        alert('标记失败：' + data.message);"; // 显示失败提示和消息
        html += "      }"; // 结束if-else
        html += "    });"; // 结束then回调
        html += "}"; // 结束markAsRead()函数
        html += "</script>"; // 结束JavaScript代码块

        html += generateFooter(); // 生成页面底部
        return html; // 返回完整的HTML字符串
    }

    /**
     * 生成充值页面
     * @param userId 用户ID
     * @return String 充值页面的HTML字符串
     */
    public String generateRechargePage(int userId) {
        String html = generateHeader("账户充值"); // 生成页面头部，标题为"账户充值"
        html += "<div style='padding: 20px; max-width: 300px; margin: 0 auto;'>"; // 添加居中样式且最大宽度为300px的div容器
        html += "<h1>账户充值</h1>"; // 添加主标题
        html += "<form method='POST' action='/recharge'>"; // 创建POST表单，提交到/recharge路径
        html += "<input type='hidden' name='userId' value='" + userId + "'>"; // 隐藏字段，存储用户ID
        html += "充值金额: <input type='number' name='amount' min='1' step='0.01' required><br><br>"; // 充值金额输入框，最小值1，步长0.01，必填
        html += "<button type='submit'>确认充值</button>"; // 提交按钮
        html += "</form>"; // 结束表单
        html += "<br><a href='/main'>返回主页</a>"; // 返回主页链接
        html += "</div>"; // 结束div容器
        html += generateFooter(); // 生成页面底部
        return html; // 返回完整的HTML字符串
    }

    /**
     * 生成成功页面
     * @param message 成功消息
     * @param redirectPage 重定向页面路径
     * @return String 成功页面的HTML字符串
     */
    public String generateSuccessPage(String message, String redirectPage) {
        String html = generateHeader("操作成功"); // 生成页面头部，标题为"操作成功"
        html += "<div style='padding: 20px; text-align: center;'>"; // 添加居中样式的div容器
        html += "<h1>成功</h1>"; // 添加主标题
        html += "<p>" + message + "</p>"; // 显示成功消息
        html += "<br><a href='" + redirectPage + "'>点击继续</a>"; // 跳转链接
        html += "</div>"; // 结束div容器
        html += generateFooter(); // 生成页面底部
        return html; // 返回完整的HTML字符串
    }

    /**
     * 生成错误页面
     * @param message 错误消息
     * @return String 错误页面的HTML字符串
     */
    public String generateErrorPage(String message) {
        String html = generateHeader("错误"); // 生成页面头部，标题为"错误"
        html += "<div style='padding: 20px; text-align: center;'>"; // 添加居中样式的div容器
        html += "<h1>错误</h1>"; // 添加主标题
        html += "<p>" + message + "</p>"; // 显示错误消息
        html += "<br><a href='/main'>返回主页</a>"; // 返回主页链接
        html += "</div>"; // 结束div容器
        html += generateFooter(); // 生成页面底部
        return html; // 返回完整的HTML字符串
    }

    /**
     * 生成页面头部
     * @param title 页面标题
     * @return String 页面头部的HTML字符串
     */
    private String generateHeader(String title) {
        return "<!DOCTYPE html>" + // HTML5文档类型声明
                "<html>" + // 开始html标签
                "<head>" + // 开始head标签
                "<meta charset='UTF-8'>" + // 设置字符编码为UTF-8
                "<title>" + title + "</title>" + // 设置页面标题
                "<style>" + // 开始样式标签
                "body { font-family: Arial, sans-serif; margin: 0; padding: 0; }" + // 设置body字体、外边距和内边距
                "header { background: #333; color: white; padding: 10px 20px; }" + // 设置header背景色、文字颜色和内边距
                "a { color: #0066cc; text-decoration: none; }" + // 设置链接颜色和无下划线
                "a:hover { text-decoration: underline; }" + // 设置鼠标悬停时链接显示下划线
                "table { border-collapse: collapse; width: 100%; }" + // 设置表格边框合并和宽度
                "th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }" + // 设置表格单元格边框、内边距和文本左对齐
                "th { background-color: #f2f2f2; }" + // 设置表头背景色
                "</style>" + // 结束样式标签
                "</head>" + // 结束head标签
                "<body>" + // 开始body标签
                "<header><h1>彩票系统</h1></header>"; // 添加header，包含系统标题
    }

    /**
     * 生成页面底部
     * @return String 页面底部的HTML字符串
     */
    private String generateFooter() {
        return "<footer style='margin-top: 20px; padding: 10px; background: #eee; text-align: center;'>" + // 添加footer，设置样式
                "彩票系统 © 2023" + // 版权信息
                "</footer>" + // 结束footer
                "</body>" + // 结束body标签
                "</html>"; // 结束html标签
    }
}