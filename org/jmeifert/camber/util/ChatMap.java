package org.jmeifert.camber.util;

/**
 * ChatMap stores program constants that aren't defined in the user configuration.
 */
public class ChatMap {
    // Log level (0: Info, 1: Warning, 2: Error, 3: None)
    public static final int LOG_LEVEL = 0;

    // Max concurrent connections (count)
    public static final int MAX_CONCURRENT_CONNECTIONS = 1000;

    // Size of the message cache for each group (count)
    public static final int GROUP_MESSAGE_CACHE_SIZE = 500;

    // Max amount of recent messages to show to clients (count)
    public static final int MAX_MESSAGES_TO_OUTPUT = 100;

    // Server thread inactivity timeout (ms = s * 1000)
    public static final int INACTIVITY_TIMEOUT = 300 * 1000;

    // Max message length (characters)
    public static final int MAX_MESSAGE_LENGTH = 400;

    // Max name length (characters)
    public static final int MAX_NAME_LENGTH = 40;

    // RSA key size (bits)
    public static final int RSA_KEY_SIZE = 2048;

    // AES key size (bits)
    public static final int AES_KEY_SIZE = 256;

}
