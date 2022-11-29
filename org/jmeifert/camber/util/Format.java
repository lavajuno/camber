package org.jmeifert.camber.util;

/**
 * Format provides functions related to string checking and sanitization.
 */
public class Format {
    /**
     * Checks if a string is alphanumeric (only contains A-Z, a-z, 0-9, and whitespace).
     * Helpful for simple sanitization.
     * @param s String to check
     * @return Returns true if the string is alphanumeric.
     */
    public static boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^[a-zA-Z0-9 ]*$");
    }

    /**
     * Checks if a string contains only safe ASCII characters (letters, numbers, special chars, newline).
     * @param s String to check
     * @return Returns true if the string is safe ASCII
     */
    public static boolean isSafeAscii(String s) {
        return s != null && s.matches("^[ -~\n]*$");
    }

    /**
     * Checks if a string is a valid message (only safe ascii, not longer than max message length)
     * @param s String to check
     * @return Returns true if the string is a valid message
     */
    public static boolean isValidMessage(String s) {
        if(s == null) {
            return false;
        }
        return s.length() <= ChatMap.MAX_MESSAGE_LENGTH && s.matches("^[ -~\n]*$");
    }

    /**
     * Checks if a string is a valid name (only safe ascii, no newlines, not longer than max name length)
     * @param s String to check
     * @return Returns true if the string is a valid name
     */
    public static boolean isValidName(String s) {
        if(s == null) {
            return false;
        }
        return s.length() <= ChatMap.MAX_NAME_LENGTH && s.matches("^[ -~]*$");
    }

    /**
     * Checks if a password hash is valid (A-Za-z0-9, length is 64)
     * @param s Password hash to check
     * @return Returns true if the string is a valid password hash
     */
    public static boolean isValidPasswordHash(String s) {
        return s.length() == 64 && s.matches("^[A-Za-z0-9]*$");
    }
}
