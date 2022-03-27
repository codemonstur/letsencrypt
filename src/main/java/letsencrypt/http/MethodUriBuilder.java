package letsencrypt.http;

public interface MethodUriBuilder {
    HeaderBodyBuilder head(String uri);
    HeaderBodyBuilder head(String uriFormat, Object... arguments);
    HeaderBodyBuilder get(String uri);
    HeaderBodyBuilder get(String uriFormat, Object... arguments);
    HeaderBodyBuilder post(String uri);
    HeaderBodyBuilder post(String uriFormat, Object... arguments);
    HeaderBodyBuilder put(String uri);
    HeaderBodyBuilder put(String uriFormat, Object... arguments);
    HeaderBodyBuilder delete(String uri);
    HeaderBodyBuilder delete(String uriFormat, Object... arguments);
    HeaderBodyBuilder method(String method, String uri);
    HeaderBodyBuilder method(String method, String uriFormat, Object... arguments);
}
