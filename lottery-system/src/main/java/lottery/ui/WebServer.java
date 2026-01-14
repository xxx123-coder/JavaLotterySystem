package lottery.ui;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * 简单的Web服务器
 */
public class WebServer {
    private HttpServer server;
    private ServletHandler servletHandler;
    private int port = 8080;
    private boolean isRunning = false;

    public void setServletHandler(ServletHandler servletHandler) {
        this.servletHandler = servletHandler;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 启动Web服务器
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        isRunning = true;
        System.out.println("Web服务器已启动，访问地址: http://localhost:" + port);
    }

    /**
     * 停止Web服务器
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            isRunning = false;
            System.out.println("Web服务器已停止");
        }
    }

    /**
     * 根路径处理器
     */
    private class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (servletHandler == null) {
                String response = "服务器未初始化";
                exchange.sendResponseHeaders(500, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            // 将HttpExchange转换为ServletRequest/Response
            // 这里简化处理，实际应该适配
            servletHandler.handleRequest(exchange);
        }
    }
}