package letsencrypt.http;

import java.net.http.HttpRequest.BodyPublisher;

public interface HeaderBodyBuilder extends ExpectBuilder {
    HeaderBodyBuilder header(String name, String value);
    ExpectBuilder body(String body);
    ExpectBuilder body(BodyPublisher body);
    ExpectBuilder jsonBody(Object body);
}
