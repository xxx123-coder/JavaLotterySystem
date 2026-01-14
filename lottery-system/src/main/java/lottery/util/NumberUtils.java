package lottery.util;

import java.util.HashSet;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 数字处理工具类
 * 提供随机数生成、号码验证、中奖判断等功能
 * 这个类是工具类，只包含静态方法，不可实例化
 */
public class NumberUtils {

    // 私有构造函数，防止工具类被实例化
    // 如果尝试实例化，会抛出IllegalStateException异常
    private NumberUtils() {
        throw new IllegalStateException("工具类不可实例化");
    }

    // 静态随机数生成器，用于生成随机数
    private static final Random RANDOM = new Random();
    // 正则表达式模式，用于验证号码字符串格式（数字用逗号分隔）
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("^\\d+(,\\d+)*$");

    // 中奖等级配置数组，索引表示匹配的数量，值表示对应的中奖等级
    private static final String[] PRIZE_LEVELS = {
            "未中奖", "未中奖", "未中奖", "未中奖", "三等奖", "二等奖", "一等奖", "特等奖"
    };

    /**
     * 生成指定数量的不重复随机数
     * @param count 要生成的随机数数量
     * @param min 随机数的最小值（包含）
     * @param max 随机数的最大值（包含）
     * @return 包含不重复随机数的整数数组
     * @throws IllegalArgumentException 如果参数无效会抛出异常
     */
    public static int[] generateRandomNumbers(int count, int min, int max) {
        // 参数验证：数量必须大于0
        if (count <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        // 参数验证：最小值不能大于最大值
        if (min > max) {
            throw new IllegalArgumentException("最小值不能大于最大值");
        }
        // 参数验证：数字范围必须足够生成指定数量的不重复数字
        if (count > (max - min + 1)) {
            throw new IllegalArgumentException("数字范围不足以生成指定数量的不重复数字");
        }

        // 使用HashSet确保数字不重复
        HashSet<Integer> set = new HashSet<>();
        // 创建结果数组
        int[] result = new int[count];
        int index = 0;

        // 循环生成随机数，直到生成足够数量的不重复数字
        while (set.size() < count) {
            // 生成[min, max]范围内的随机整数
            int num = RANDOM.nextInt(max - min + 1) + min;
            // 如果数字不重复，添加到结果数组
            if (set.add(num)) {
                result[index++] = num;
            }
        }

        // 返回结果数组
        return result;
    }

    /**
     * 验证号码字符串格式是否正确
     * @param numbersStr 要验证的号码字符串，格式如"1,2,3,4,5,6,7"
     * @return 验证结果，true表示格式正确
     */
    public static boolean isValidNumbers(String numbersStr) {
        // 如果字符串为空或只包含空格，返回false
        if (numbersStr == null || numbersStr.trim().isEmpty()) {
            return false;
        }

        // 使用正则表达式验证字符串格式：数字用逗号分隔
        if (!NUMBERS_PATTERN.matcher(numbersStr).matches()) {
            return false;
        }

        // 按逗号分割字符串，得到各个数字部分
        String[] parts = numbersStr.split(",");
        // 彩票系统要求正好7个数字
        if (parts.length != 7) {
            return false;
        }

        // 使用HashSet检查数字是否重复
        HashSet<Integer> set = new HashSet<>();
        for (String part : parts) {
            try {
                // 将字符串转换为整数
                int num = Integer.parseInt(part.trim());
                // 验证数字范围在1-36之间
                if (num < 1 || num > 36) {
                    return false;
                }
                // 验证数字是否重复
                if (!set.add(num)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                // 如果转换失败，说明包含非数字字符
                return false;
            }
        }

        // 所有验证通过，返回true
        return true;
    }

    /**
     * 将数字数组转换为字符串
     * @param numbers 要转换的数字数组
     * @return 转换后的字符串，格式如"1,2,3,4,5,6,7"
     */
    public static String numbersToString(int[] numbers) {
        // 如果数组为空或长度为0，返回空字符串
        if (numbers == null || numbers.length == 0) {
            return "";
        }

        // 使用StringBuilder高效构建字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numbers.length; i++) {
            // 添加数字
            sb.append(numbers[i]);
            // 如果不是最后一个数字，添加逗号分隔符
            if (i < numbers.length - 1) {
                sb.append(",");
            }
        }

        // 返回构建的字符串
        return sb.toString();
    }

    /**
     * 将字符串转换为数字数组
     * @param numbersStr 要转换的字符串，格式如"1,2,3,4,5,6,7"
     * @return 转换后的数字数组
     * @throws IllegalArgumentException 如果字符串包含非数字字符
     */
    public static int[] stringToNumbers(String numbersStr) {
        // 如果字符串为空或只包含空格，返回空数组
        if (numbersStr == null || numbersStr.trim().isEmpty()) {
            return new int[0];
        }

        // 按逗号分割字符串
        String[] parts = numbersStr.split(",");
        // 创建结果数组，长度等于分割后的部分数
        int[] numbers = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                // 将每个部分转换为整数，并存储到数组
                numbers[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                // 如果转换失败，抛出异常
                throw new IllegalArgumentException("字符串包含非数字字符: " + parts[i]);
            }
        }

        // 返回结果数组
        return numbers;
    }

    /**
     * 计算彩票号码与中奖号码的匹配数量
     * @param ticketNumbers 彩票号码数组
     * @param winningNumbers 中奖号码数组
     * @return 匹配的数量
     */
    public static int countMatches(int[] ticketNumbers, int[] winningNumbers) {
        // 如果任一数组为空，返回0
        if (ticketNumbers == null || winningNumbers == null) {
            return 0;
        }

        int matchCount = 0;

        // 使用双重循环比较每个数字
        // 注意：这个方法的时间复杂度是O(n*m)，对于小数组可以接受
        for (int ticketNum : ticketNumbers) {
            for (int winningNum : winningNumbers) {
                // 如果找到匹配的数字，增加计数并跳出内层循环
                if (ticketNum == winningNum) {
                    matchCount++;
                    break;
                }
            }
        }

        // 返回匹配数量
        return matchCount;
    }

    /**
     * 根据匹配数量获取中奖等级
     * @param matchCount 匹配数量
     * @return 中奖等级字符串
     */
    public static String getPrizeLevel(int matchCount) {
        // 验证匹配数量是否在有效范围内
        if (matchCount < 0 || matchCount >= PRIZE_LEVELS.length) {
            return "未中奖";
        }
        // 从数组中获取对应的中奖等级
        return PRIZE_LEVELS[matchCount];
    }

    /**
     * 检查数组中的数字是否都是唯一的
     * @param numbers 要检查的数字数组
     * @return 如果所有数字都不重复则返回true，否则返回false
     */
    public static boolean isUniqueNumbers(int[] numbers) {
        // 如果数组为空，返回false
        if (numbers == null) {
            return false;
        }

        // 使用HashSet检查数字是否重复
        HashSet<Integer> set = new HashSet<>();
        for (int num : numbers) {
            // 如果HashSet中已包含该数字，说明有重复
            if (set.contains(num)) {
                return false;
            }
            // 将数字添加到HashSet
            set.add(num);
        }
        // 所有数字都不重复
        return true;
    }

    /**
     * 验证彩票号码字符串并转换为数组
     * @param numbersStr 要验证的号码字符串
     * @return 验证通过的号码数组
     * @throws IllegalArgumentException 如果验证失败
     */
    public static int[] validateAndConvertNumbers(String numbersStr) {
        // 先验证字符串格式
        if (!isValidNumbers(numbersStr)) {
            throw new IllegalArgumentException("号码格式无效。必须为7个1-36之间的不重复数字，用逗号分隔");
        }

        // 验证通过后转换为数组
        return stringToNumbers(numbersStr);
    }

    /**
     * 生成一组随机彩票号码字符串
     * @return 随机号码字符串，格式如"1,2,3,4,5,6,7"
     */
    public static String generateRandomNumbersString() {
        // 生成7个1-36之间的随机数
        int[] numbers = generateRandomNumbers(7, 1, 36);
        // 将数组转换为字符串
        return numbersToString(numbers);
    }

    /**
     * 格式化号码字符串，确保格式一致
     * @param numbersStr 原始号码字符串
     * @return 格式化后的号码字符串
     */
    public static String formatNumbersString(String numbersStr) {
        // 如果字符串为空，返回空字符串
        if (numbersStr == null) {
            return "";
        }

        // 将字符串转换为数组，再转换回字符串，以去除多余空格等
        int[] numbers = stringToNumbers(numbersStr);
        return numbersToString(numbers);
    }
}