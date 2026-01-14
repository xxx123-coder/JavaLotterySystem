package lottery.dao; // 定义包路径，表明该接口属于lottery.dao包

// 导入模型类
import lottery.model.User; // 导入用户模型类
import lottery.model.Ticket; // 导入彩票模型类
import lottery.model.LotteryResult; // 导入开奖结果模型类
import java.util.List; // 导入List接口，用于返回列表数据

/**
 * Excel数据访问接口
 * 定义了Excel数据访问层的基本操作，提供统一的接口规范
 */
public interface IExcelDao {

    /**
     * 设置调试模式
     * 开启或关闭调试模式，用于输出调试信息
     * @param debug 调试模式标志，true为开启，false为关闭
     */
    void setDebugMode(boolean debug);

    /**
     * 创建Excel文件
     * 初始化时创建所有需要的Excel数据文件
     */
    void createExcelFiles();

    /**
     * 加载用户数据
     * 从Excel文件中读取所有用户数据
     * @return 用户对象列表
     */
    List<User> loadUsers();

    /**
     * 加载彩票数据
     * 从Excel文件中读取所有彩票数据
     * @return 彩票对象列表
     */
    List<Ticket> loadTickets();

    /**
     * 加载开奖结果
     * 从Excel文件中读取所有开奖结果数据
     * @return 开奖结果对象列表
     */
    List<LotteryResult> loadResults();

    /**
     * 保存用户数据
     * 将用户数据保存到Excel文件中
     * @param users 用户对象列表
     */
    void saveUsers(List<User> users);

    /**
     * 保存彩票数据
     * 将彩票数据保存到Excel文件中
     * @param tickets 彩票对象列表
     */
    void saveTickets(List<Ticket> tickets);

    /**
     * 保存开奖结果
     * 将开奖结果数据保存到Excel文件中
     * @param results 开奖结果对象列表
     */
    void saveResults(List<LotteryResult> results);

    /**
     * 获取下一个ID
     * 根据表名获取下一个可用的ID值，用于新记录的插入
     * @param tableName 表名（如"users"、"tickets"、"results"）
     * @return 下一个可用ID
     */
    int getNextId(String tableName);

    /**
     * 添加用户
     * 向Excel文件中添加一个新用户
     * @param user 用户对象
     */
    void addUser(User user);

    /**
     * 添加彩票
     * 向Excel文件中添加一张新彩票
     * @param ticket 彩票对象
     */
    void addTicket(Ticket ticket);

    /**
     * 添加开奖结果
     * 向Excel文件中添加一个新的开奖结果
     * @param result 开奖结果对象
     */
    void addResult(LotteryResult result);
}