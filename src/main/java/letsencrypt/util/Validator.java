package letsencrypt.util;

import java.net.URLEncoder;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public enum Validator {;

    public static String requireNotEmpty(final String value, final String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value;
    }

    public static <T> List<T> requireNotEmpty(final List<T> list, final String message) {
        if (list == null || list.isEmpty()) throw new IllegalArgumentException(message);
        return list;
    }

    public static String urlEncode(final String value) {
        return value != null ? URLEncoder.encode(value, UTF_8) : "";
    }

}
