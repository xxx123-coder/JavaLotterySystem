package lottery.dao;

import lottery.model.User;
import lottery.model.Ticket;
import lottery.model.LotteryResult;
import java.util.List;

/**
 * Excel数据访问接口
 */
public interface IExcelDao {

    /**
     * 设置调试模式
     */
    void setDebugMode(boolean debug);

    /**
     * 创建Excel文件
     */
    void createExcelFiles();

    /**
     * 加载用户数据
     */
    List<User> loadUsers();

    /**
     * 加载彩票数据
     */
    List<Ticket> loadTickets();

    /**
     * 加载开奖结果
     */
    List<LotteryResult> loadResults();

    /**
     * 保存用户数据
     */
    void saveUsers(List<User> users);

    /**
     * 保存彩票数据
     */
    void saveTickets(List<Ticket> tickets);

    /**
     * 保存开奖结果
     */
    void saveResults(List<LotteryResult> results);

    /**
     * 获取下一个ID
     */
    int getNextId(String tableName);

    /**
     * 添加用户
     */
    void addUser(User user);

    /**
     * 添加彩票
     */
    void addTicket(Ticket ticket);

    /**
     * 添加开奖结果
     */
    void addResult(LotteryResult result);
}