package com.qrcode.attendance.util;

import java.util.logging.Logger;

public class LoggerUtil {
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}