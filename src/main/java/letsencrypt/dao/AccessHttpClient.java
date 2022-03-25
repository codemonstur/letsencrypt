package letsencrypt.dao;

import java.net.http.HttpClient;

public interface AccessHttpClient {
    HttpClient getHttpClient();
}
