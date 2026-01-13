package lottery.ui;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * 彩票系统Web服务器启动类
 * 使用嵌入式Jetty服务器
 */
public class WebServer {
    private Server server;
    private int PORT = 8080;

    /**
     * 启动Web服务器
     */
    public void start() {
        try {
            server = new Server(PORT);
            configureServer();
            server.start();
            System.out.println("彩票系统Web服务器已启动，访问地址: http://localhost:" + PORT);
            server.join();
        } catch (Exception e) {
            System.err.println("启动服务器失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 停止Web服务器
     */
    public void stop() {
        try {
            if (server != null && server.isRunning()) {
                server.stop();
                System.out.println("服务器已停止");
            }
        } catch (Exception e) {
            System.err.println("停止服务器失败: " + e.getMessage());
        }
    }

    /**
     * 检查服务器状态
     * @return 服务器是否正在运行
     */
    public boolean isRunning() {
        return server != null && server.isRunning();
    }

    /**
     * 配置服务器和处理器
     */
    public void configureServer() {
        // 创建Servlet上下文处理器
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // 添加Servlet处理器
        context.addServlet(new ServletHolder(new ServletHandler()), "/*");

        // 设置处理器
        server.setHandler(context);
    }

    /**
     * 设置服务器端口
     * @param port 端口号
     */
    public void setPort(int port) {
        if (port > 0 && port < 65536) {
            this.PORT = port;
        } else {
            System.err.println("端口号无效，使用默认端口8080");
        }
    }

    /**
     * 程序主入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        WebServer webServer = new WebServer();

        // 解析命令行参数，例如设置端口
        if (args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                webServer.setPort(port);
            } catch (NumberFormatException e) {
                System.err.println("端口参数格式错误，使用默认端口8080");
            }
        }

        // 启动服务器
        webServer.start();
    }
}