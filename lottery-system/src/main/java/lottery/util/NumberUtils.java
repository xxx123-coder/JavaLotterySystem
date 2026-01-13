package lottery.util;

import java.util.HashSet;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 数字处理工具类
 * 提供随机数生成、号码验证、中奖判断等功能
 */
public class NumberUtils {

    // 私有构造函数，防止实例化
    private NumberUtils() {
        throw new IllegalStateException("工具类不可实例化");
    }

    private static final Random RANDOM = new Random();
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("^\\d+(,\\d+)*$");

    // 中奖等级配置
    private static final String[] PRIZE_LEVELS = {
            "未中奖", "未中奖", "未中奖", "未中奖", "三等奖", "二等奖", "一等奖", "特等奖"
    };

    /**
     * 生成指定数量的不重复随机数
     * @param count 数量
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 不重复的随机数数组
     */
    public static int[] generateRandomNumbers(int count, int min, int max) {
        // 参数验证
        if (count <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        if (min > max) {
            throw new IllegalArgumentException("最小值不能大于最大值");
        }
        if (count > (max - min + 1)) {
            throw new IllegalArgumentException("数字范围不足以生成指定数量的不重复数字");
        }

        HashSet<Integer> set = new HashSet<>();
        int[] result = new int[count];
        int index = 0;

        while (set.size() < count) {
            int num = RANDOM.nextInt(max - min + 1) + min;
            if (set.add(num)) {
                result[index++] = num;
            }
        }

        return result;
    }

    /**
     * 验证号码字符串格式
     * @param numbersStr 号码字符串，格式如"1,2,3,4,5,6,7"
     * @return 验证结果
     */
    public static boolean isValidNumbers(String numbersStr) {
        if (numbersStr == null || numbersStr.trim().isEmpty()) {
            return false;
        }

        // 验证格式：数字用逗号分隔
        if (!NUMBERS_PATTERN.matcher(numbersStr).matches()) {
            return false;
        }

        // 拆分数字
        String[] parts = numbersStr.split(",");
        if (parts.length != 7) {
            return false; // 必须正好7个数字
        }

        HashSet<Integer> set = new HashSet<>();
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part.trim());
                // 验证范围1-36
                if (num < 1 || num > 36) {
                    return false;
                }
                // 验证不重复
                if (!set.add(num)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将数字数组转换为字符串
     * @param numbers 数字数组
     * @return 字符串格式，如"1,2,3,4,5,6,7"
     */
    public static String numbersToString(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numbers.length; i++) {
            sb.append(numbers[i]);
            if (i < numbers.length - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    /**
     * 将字符串转换为数字数组
     * @param numbersStr 字符串格式，如"1,2,3,4,5,6,7"
     * @return 数字数组
     */
    public static int[] stringToNumbers(String numbersStr) {
        if (numbersStr == null || numbersStr.trim().isEmpty()) {
            return new int[0];
        }

        String[] parts = numbersStr.split(",");
        int[] numbers = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                numbers[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("字符串包含非数字字符: " + parts[i]);
            }
        }

        return numbers;
    }

    /**
     * 计算匹配数量
     * @param ticketNumbers 彩票号码
     * @param winningNumbers 中奖号码
     * @return 匹配的数量
     */
    public static int countMatches(int[] ticketNumbers, int[] winningNumbers) {
        if (ticketNumbers == null || winningNumbers == null) {
            return 0;
        }

        int matchCount = 0;

        // 使用双重循环匹配（假设数量不大，可以接受O(n^2)复杂度）
        for (int ticketNum : ticketNumbers) {
            for (int winningNum : winningNumbers) {
                if (ticketNum == winningNum) {
                    matchCount++;
                    break;
                }
            }
        }

        return matchCount;
    }

    /**
     * 根据匹配数量返回中奖等级
     * @param matchCount 匹配数量
     * @return 中奖等级字符串
     */
    public static String getPrizeLevel(int matchCount) {
        if (matchCount < 0 || matchCount >= PRIZE_LEVELS.length) {
            return "未中奖";
        }
        return PRIZE_LEVELS[matchCount];
    }

    /**
     * 检查数组中的数字是否唯一（辅助方法）
     * @param numbers 数字数组
     * @return 是否所有数字都不重复
     */
    public static boolean isUniqueNumbers(int[] numbers) {
        if (numbers == null) {
            return false;
        }

        HashSet<Integer> set = new HashSet<>();
        for (int num : numbers) {
            if (set.contains(num)) {
                return false;
            }
            set.add(num);
        }
        return true;
    }

    /**
     * 验证彩票号码并转换为数组（综合方法）
     * @param numbersStr 号码字符串
     * @return 验证通过的号码数组
     * @throws IllegalArgumentException 验证失败时抛出异常
     */
    public static int[] validateAndConvertNumbers(String numbersStr) {
        if (!isValidNumbers(numbersStr)) {
            throw new IllegalArgumentException("号码格式无效。必须为7个1-36之间的不重复数字，用逗号分隔");
        }

        return stringToNumbers(numbersStr);
    }

    /**
     * 生成一组随机彩票号码字符串
     * @return 随机号码字符串
     */
    public static String generateRandomNumbersString() {
        int[] numbers = generateRandomNumbers(7, 1, 36);
        return numbersToString(numbers);
    }

    /**
     * 格式化号码字符串，确保格式一致
     * @param numbersStr 原始号码字符串
     * @return 格式化后的号码字符串
     */
    public static String formatNumbersString(String numbersStr) {
        if (numbersStr == null) {
            return "";
        }

        int[] numbers = stringToNumbers(numbersStr);
        return numbersToString(numbers);
    }
}