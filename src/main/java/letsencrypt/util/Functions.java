package letsencrypt.util;

import bobthebuildtool.services.Log;

import java.net.URLEncoder;

import static java.lang.System.currentTimeMillis;
import static java.nio.charset.StandardCharsets.UTF_8;

public enum Functions {;

    public static void logInfo(final boolean enabled, final String message) {
        if (enabled) Log.logInfo(message);
    }

    public static String urlEncode(final String value) {
        return value != null ? URLEncoder.encode(value, UTF_8) : "";
    }

    public static boolean isInThePast(final long stopTime) {
        return stopTime < currentTimeMillis();
    }

}
