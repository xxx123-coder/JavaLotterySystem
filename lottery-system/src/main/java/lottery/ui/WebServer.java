package lottery.ui;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import lottery.service.UserService;
import lottery.service.TicketService;
import lottery.service.LotteryService;
import lottery.util.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 彩票系统Web服务器启动类
 * 增强服务器配置和异常处理
 */
public class WebServer {
    private Server server;
    private int port = 8080;
    private String host = "localhost";

    // 服务依赖
    private UserService userService;
    private TicketService ticketService;
    private LotteryService lotteryService;

    // 服务器配置
    private int maxThreads = 200;
    private final int minThreads = 20;
    private static final int IDLE_TIMEOUT = 30000;
    private static final int CONNECTION_TIMEOUT = 30000;

    // 日志
    private static final Logger logger = Logger.getLogger(WebServer.class);

    /**
     * 启动Web服务器（增强版）
     */
    public void start() {
        try {
            logger.info("正在启动Web服务器，端口: " + port);

            // 验证服务依赖
            validateDependencies();

            // 配置线程池
            QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, IDLE_TIMEOUT);
            server = new Server(threadPool);

            // 配置连接器
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(port);
            connector.setHost(host);
            connector.setIdleTimeout(CONNECTION_TIMEOUT);
            server.addConnector(connector);

            // 配置服务器
            configureServer();

            // 启动服务器
            server.start();

            logger.info("彩票系统Web服务器已启动");
            logger.info("访问地址: http://" + host + ":" + port);
            logger.info("管理界面: http://" + host + ":" + port + "/admin");
            System.out.println("彩票系统Web服务器已启动，访问地址: http://" + host + ":" + port);

            // 等待服务器运行
            server.join();

        } catch (Exception e) {
            logger.error("启动服务器失败: " + e.getMessage(), e);
            System.err.println("启动服务器失败: " + e.getMessage());
            throw new RuntimeException("Web服务器启动失败", e);
        }
    }

    /**
     * 停止Web服务器（增强版）
     */
    public void stop() {
        try {
            if (server != null && server.isRunning()) {
                logger.info("正在停止Web服务器...");

                // 先停止接受新请求
                server.stop();

                // 等待处理中的请求完成
                server.join();

                logger.info("Web服务器已停止");
                System.out.println("服务器已停止");
            }
        } catch (Exception e) {
            logger.error("停止服务器失败: " + e.getMessage(), e);
            System.err.println("停止服务器失败: " + e.getMessage());
            throw new RuntimeException("Web服务器停止失败", e);
        }
    }

    /**
     * 检查服务器状态
     */
    public boolean isRunning() {
        return server != null && server.isRunning();
    }

    /**
     * 配置服务器和处理器（增强版）
     */
    public void configureServer() {
        try {
            // 创建Servlet上下文处理器
            ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
            servletContext.setContextPath("/");

            // 配置会话管理
            servletContext.getSessionHandler().setMaxInactiveInterval(3600); // 1小时

            // 创建资源处理器（用于静态文件）
            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirectoriesListed(false);
            resourceHandler.setWelcomeFiles(new String[]{"index.html"});

            // 设置资源目录
            String resourceBase = System.getProperty("user.dir") + "/webapp";
            resourceHandler.setResourceBase(resourceBase);

            // 注册错误处理器
            servletContext.setErrorHandler(new CustomErrorHandler());

            // 添加健康检查Servlet
            servletContext.addServlet(new ServletHolder(new HealthCheckServlet()), "/health");
            servletContext.addServlet(new ServletHolder(new AdminServlet()), "/admin/*");

            // 创建处理器列表
            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] {resourceHandler, servletContext});

            // 设置处理器
            server.setHandler(handlers);

            // 配置服务器参数
            server.setStopAtShutdown(true);
            server.setStopTimeout(5000);

            logger.info("服务器配置完成");

        } catch (Exception e) {
            logger.error("配置服务器失败: " + e.getMessage(), e);
            throw new RuntimeException("服务器配置失败", e);
        }
    }

    /**
     * 验证服务依赖
     */
    private void validateDependencies() {
        if (userService == null) {
            logger.warn("UserService未设置，部分功能可能不可用");
        }
        if (ticketService == null) {
            logger.warn("TicketService未设置，部分功能可能不可用");
        }
        if (lotteryService == null) {
            logger.warn("LotteryService未设置，部分功能可能不可用");
        }
    }

    /**
     * 设置服务器端口
     */
    public void setPort(int port) {
        if (port > 0 && port < 65536) {
            this.port = port;
            logger.info("服务器端口设置为: " + port);
        } else {
            logger.warn("端口号无效: " + port + "，使用默认端口8080");
            System.err.println("端口号无效，使用默认端口8080");
        }
    }

    /**
     * 设置服务器主机
     */
    public void setHost(String host) {
        this.host = host;
        logger.info("服务器主机设置为: " + host);
    }

    /**
     * 设置最大线程数
     */
    public void setMaxThreads(int maxThreads) {
        if (maxThreads > 0) {
            this.maxThreads = maxThreads;
            logger.info("最大线程数设置为: " + maxThreads);
        }
    }

    /**
     * 设置服务依赖
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
        logger.info("UserService已设置");
    }

    public void setTicketService(TicketService ticketService) {
        this.ticketService = ticketService;
        logger.info("TicketService已设置");
    }

    public void setLotteryService(LotteryService lotteryService) {
        this.lotteryService = lotteryService;
        logger.info("LotteryService已设置");
    }

    /**
     * 程序主入口
     */
    public static void main(String[] args) {
        try {
            WebServer webServer = new WebServer();

            // 解析命令行参数
            if (args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    switch (args[i]) {
                        case "-port":
                            if (i + 1 < args.length) {
                                try {
                                    int port = Integer.parseInt(args[i + 1]);
                                    webServer.setPort(port);
                                    i++;
                                } catch (NumberFormatException e) {
                                    logger.error("端口参数格式错误: " + args[i + 1]);
                                }
                            }
                            break;

                        case "-host":
                            if (i + 1 < args.length) {
                                webServer.setHost(args[i + 1]);
                                i++;
                            }
                            break;

                        case "-maxThreads":
                            if (i + 1 < args.length) {
                                try {
                                    int maxThreads = Integer.parseInt(args[i + 1]);
                                    webServer.setMaxThreads(maxThreads);
                                    i++;
                                } catch (NumberFormatException e) {
                                    logger.error("最大线程数参数格式错误: " + args[i + 1]);
                                }
                            }
                            break;

                        case "-help":
                            printHelp();
                            return;

                        default:
                            logger.warn("未知参数: " + args[i]);
                            break;
                    }
                }
            }

            logger.info("启动Web服务器...");
            System.out.println("启动Web服务器，端口: " + webServer.port);

            // 启动服务器
            webServer.start();

        } catch (Exception e) {
            logger.error("Web服务器启动失败: " + e.getMessage(), e);
            System.err.println("Web服务器启动失败: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * 打印帮助信息
     */
    private static void printHelp() {
        System.out.println("Web服务器使用说明:");
        System.out.println();
        System.out.println("命令行参数:");
        System.out.println("  -port <端口号>       设置服务器端口 (默认: 8080)");
        System.out.println("  -host <主机名>       设置服务器主机 (默认: localhost)");
        System.out.println("  -maxThreads <数量>   设置最大线程数 (默认: 200)");
        System.out.println("  -help                显示此帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -cp . lottery.ui.WebServer -port 9090 -host 0.0.0.0");
        System.out.println();
    }
}

/**
 * 自定义错误处理器
 */
class CustomErrorHandler extends org.eclipse.jetty.server.handler.ErrorHandler {
    private static final Logger logger = Logger.getLogger(CustomErrorHandler.class);

    @Override
    public void handle(String target, org.eclipse.jetty.server.Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {

        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        logger.warn("HTTP错误 " + status + ": " + method + " " + uri);

        // 根据状态码返回不同的错误页面
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write("<html><body><h1>错误 " + status + "</h1>");

        switch (status) {
            case 404:
                response.getWriter().write("<p>请求的资源不存在: " + uri + "</p>");
                break;
            case 500:
                response.getWriter().write("<p>服务器内部错误，请联系管理员</p>");
                break;
            case 403:
                response.getWriter().write("<p>访问被拒绝</p>");
                break;
            default:
                response.getWriter().write("<p>未知错误</p>");
                break;
        }

        response.getWriter().write("</body></html>");
    }
}

/**
 * 健康检查Servlet
 */
class HealthCheckServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(HealthCheckServlet.class);

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 简单的健康检查响应
        String status = "{\"status\":\"UP\",\"timestamp\":\"" +
                new Date() + "\"}";

        response.getWriter().write(status);
        response.setStatus(200);

        logger.debug("健康检查请求");
    }
}

/**
 * 管理界面Servlet
 */
class AdminServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AdminServlet.class);

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        String path = request.getPathInfo();

        if (path == null || path.equals("/") || path.equals("/dashboard")) {
            showDashboard(response);
        } else if (path.equals("/status")) {
            showStatus(response);
        } else if (path.equals("/logs")) {
            showLogs(response);
        } else {
            response.sendError(404, "管理页面不存在: " + path);
        }
    }

    private void showDashboard(HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html;charset=utf-8");

        String html = "<html><head><title>彩票系统管理界面</title></head><body>" +
                "<h1>彩票系统管理界面</h1>" +
                "<ul>" +
                "<li><a href=\"/admin/status\">系统状态</a></li>" +
                "<li><a href=\"/admin/logs\">日志查看</a></li>" +
                "</ul>" +
                "</body></html>";

        response.getWriter().write(html);

        logger.info("访问管理界面");
    }

    private void showStatus(HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");

        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", new Date());
        status.put("memory", new HashMap<String, Object>() {{
            put("total", runtime.totalMemory());
            put("free", runtime.freeMemory());
            put("used", runtime.totalMemory() - runtime.freeMemory());
            put("max", runtime.maxMemory());
        }});

        // 转换为JSON（简化实现）
        String json = "{\"status\":\"ok\",\"data\":" + status + "}";
        response.getWriter().write(json);

        logger.debug("状态检查请求");
    }

    private void showLogs(HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write("<html><body><h1>日志查看功能开发中...</h1></body></html>");
    }
}