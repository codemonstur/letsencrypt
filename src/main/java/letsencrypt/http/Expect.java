package letsencrypt.http;

import java.io.IOException;
import java.net.http.HttpResponse;

public interface Expect<T> {
    void verify(HttpResponse<T> response) throws IOException;
}