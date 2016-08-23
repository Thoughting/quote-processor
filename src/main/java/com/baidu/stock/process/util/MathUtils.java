package com.baidu.stock.process.util;

import java.math.BigDecimal;

public class MathUtils {

    // 四舍五入, 取指定小数位数
    public static float leaveDigits(double value, int digits) {
        BigDecimal bg = new BigDecimal(value);
        return bg.setScale(digits, BigDecimal.ROUND_HALF_UP).floatValue();
    }

}
