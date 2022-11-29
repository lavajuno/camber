package org.jmeifert.camber.file;

import org.jmeifert.camber.util.ChatMap;
import java.util.Date;

public class Log {
    public static final int LOG_LEVEL = ChatMap.LOG_LEVEL;

    /**
     * Logs an event. Default severity is 0 (OK).
     * @param message Message to log
     */
    public static void log(String message) {
        if(LOG_LEVEL <= 0) {
            System.out.println("[  OK  ] (" + new Date() + ") " + message);
        }
    }

    /**
     * Logs an event.
     * @param message Message to log
     * @param level Event severity (0: OK, 1: WARN, 2: ERROR!)
     */
    public static void log(String message, int level) {
        if (level < LOG_LEVEL) { return; }
        if (level == 0) {
            System.out.println("[  OK  ] (" + new Date() + ") " + message);
        } else if (level == 1) {
            System.out.println("[ WARN ] (" + new Date() + ") " + message);
        } else {
            System.out.println("[ERROR!] (" + new Date() + ") " + message);
        }
    }
}
