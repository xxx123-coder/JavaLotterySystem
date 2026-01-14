package lottery.filter; // 定义包路径，表明该类属于lottery.filter包，负责过滤器功能

// 导入必要的Servlet API类
import javax.servlet.*; // 导入Servlet过滤器相关接口和类
import java.io.IOException; // 导入输入输出异常类

/**
 * 编码过滤器类
 * 用于统一设置请求和响应的字符编码，解决中文乱码问题
 */
public class EncodingFilter implements Filter { // 实现Filter接口
    private String encoding = "UTF-8"; // 默认字符编码，初始化为UTF-8

    /**
     * 过滤器初始化方法
     * 从web.xml配置文件中读取encoding参数
     * @param filterConfig 过滤器配置对象，包含初始化参数
     */
    @Override
    public void init(FilterConfig filterConfig) { // 实现Filter接口的init方法
        String encodingParam = filterConfig.getInitParameter("encoding"); // 从web.xml获取encoding参数
        if (encodingParam != null) { // 如果配置了编码参数
            encoding = encodingParam; // 使用配置的编码替换默认编码
        }
    }

    /**
     * 过滤器核心方法
     * 设置请求和响应的字符编码，然后继续过滤器链
     * @param request Servlet请求对象
     * @param response Servlet响应对象
     * @param chain 过滤器链对象
     * @throws IOException 输入输出异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException { // 实现doFilter方法
        request.setCharacterEncoding(encoding); // 设置请求的字符编码
        response.setCharacterEncoding(encoding); // 设置响应的字符编码
        chain.doFilter(request, response); // 继续执行过滤器链中的下一个过滤器或目标资源
    }

    /**
     * 过滤器销毁方法
     * 在过滤器生命周期结束时调用，用于清理资源
     */
    @Override
    public void destroy() { // 实现destroy方法
        // 清理资源
    }
}