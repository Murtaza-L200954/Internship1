package org.example.demo1.common;

import org.slf4j.Logger;
import org.slf4j.MDC;
import java.util.UUID;

public class LogUtil {

    public static void setMDC(int userId){
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("userId", String.valueOf(userId));
    }

    public static void clearMDC(){
        MDC.clear();
    }

    public static void logInfo(Logger logger, String message) {
        String userId = MDC.get("userId");
        String requestId = MDC.get("requestId");
        logger.info("[userId={} | requestId={}] {}", userId, requestId, message);
    }

    public static void logError(Logger logger, String message, Throwable throwable) {
        String prefix = String.format("[userId=%s | requestId=%s] ", MDC.get("userId"), MDC.get("requestId"));
        logger.error(prefix + message, throwable);
    }

    public static void logWarn(Logger logger, String message, Throwable throwable) {
        String prefix = String.format("[userId=%s | requestId=%s] ", MDC.get("userId"), MDC.get("requestId"));
        logger.warn(prefix + message, throwable);
    }
}
