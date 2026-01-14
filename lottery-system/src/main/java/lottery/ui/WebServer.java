package lottery.ui; // 定义包路径，表明该类属于lottery.ui包，负责Web服务器

// 导入必要的类和包
import com.sun.net.httpserver.HttpServer; // 导入HTTP服务器类
import com.sun.net.httpserver.HttpHandler; // 导入HTTP处理器接口
import com.sun.net.httpserver.HttpExchange; // 导入HTTP交换类
import java.io.IOException; // 导入输入输出异常类
import java.io.OutputStream; // 导入输出流类
import java.net.InetSocketAddress; // 导入网络套接字地址类
import java.util.concurrent.Executors; // 导入线程池执行器类

/**
 * 简单的Web服务器
 * 基于Java内置的HTTP服务器，提供Web服务
 */
public class WebServer {
    private HttpServer server; // HTTP服务器实例
    private ServletHandler servletHandler; // Servlet处理器实例
    private int port = 8080; // 服务器端口，默认为8080
    private boolean isRunning = false; // 服务器运行状态标志

    /**
     * 设置Servlet处理器
     * @param servletHandler Servlet处理器实例
     */
    public void setServletHandler(ServletHandler servletHandler) {
        this.servletHandler = servletHandler; // 设置Servlet处理器
    }

    /**
     * 设置端口
     * @param port 端口号
     */
    public void setPort(int port) {
        this.port = port; // 设置端口
    }

    /**
     * 获取端口
     * @return int 当前端口号
     */
    public int getPort() {
        return port; // 返回端口
    }

    /**
     * 检查服务器是否正在运行
     * @return boolean 运行状态
     */
    public boolean isRunning() {
        return isRunning; // 返回运行状态
    }

    /**
     * 启动Web服务器
     * @throws IOException 输入输出异常
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0); // 创建HTTP服务器，绑定指定端口，backlog为0
        server.createContext("/", new RootHandler()); // 为根路径创建上下文，使用RootHandler处理
        server.setExecutor(Executors.newFixedThreadPool(10)); // 设置线程池，最大10个线程
        server.start(); // 启动服务器
        isRunning = true; // 设置运行状态为true
        System.out.println("Web服务器已启动，访问地址: http://localhost:" + port); // 输出启动信息
    }

    /**
     * 停止Web服务器
     */
    public void stop() {
        if (server != null) { // 如果服务器不为空
            server.stop(0); // 停止服务器，延迟0秒
            isRunning = false; // 设置运行状态为false
            System.out.println("Web服务器已停止"); // 输出停止信息
        }
    }

    /**
     * 根路径处理器
     * 内部类，实现HttpHandler接口，处理所有HTTP请求
     */
    private class RootHandler implements HttpHandler {
        /**
         * 处理HTTP请求
         * @param exchange HTTP交换对象
         * @throws IOException 输入输出异常
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (servletHandler == null) { // 如果Servlet处理器为空
                String response = "服务器未初始化"; // 错误消息
                exchange.sendResponseHeaders(500, response.getBytes().length); // 发送500状态码和响应头
                OutputStream os = exchange.getResponseBody(); // 获取响应输出流
                os.write(response.getBytes()); // 写入错误消息
                os.close(); // 关闭输出流
                return; // 返回
            }

            // 将HttpExchange转换为ServletRequest/Response
            // 这里简化处理，实际应该适配
            servletHandler.handleRequest(exchange); // 调用Servlet处理器处理请求
        }
    }
}