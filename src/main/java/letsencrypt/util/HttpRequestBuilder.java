package letsencrypt.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.net.http.HttpClient.newHttpClient;

public class HttpRequestBuilder<T> {

    public static final HttpClient HTTP = newHttpClient();

    public static HttpRequestBuilder<String> newHttpRequest() {
        return new HttpRequestBuilder<>(HTTP, BodyHandlers.ofString());
    }
    public static HttpRequestBuilder<String> newHttpRequest(final HttpClient http) {
        return new HttpRequestBuilder<>(http, BodyHandlers.ofString());
    }
    public static <T> HttpRequestBuilder<T> newHttpRequest(final BodyHandler<T> handler) {
        return new HttpRequestBuilder<>(HTTP, handler);
    }
    public static <T> HttpRequestBuilder<T> newHttpRequest(final HttpClient http, final BodyHandler<T> handler) {
        return new HttpRequestBuilder<>(http, handler);
    }

    protected final HttpClient http;
    protected final HttpRequest.Builder request;
    protected final BodyHandler<T> handler;
    private List<Expect<T>> expects;

    public HttpRequestBuilder(final HttpClient http, final BodyHandler<T> handler) {
        this.http = http;
        this.request = HttpRequest.newBuilder();
        this.handler = handler;
    }

    public HttpRequestBuilder<T> uri(final String uri) {
        return uri(URI.create(uri));
    }
    public HttpRequestBuilder<T> uri(final URI uri) {
        request.uri(uri);
        return this;
    }
    public HttpRequestBuilder<T> method(final String method, final BodyPublisher publisher) {
        request.method(method, publisher);
        return this;
    }
    public HttpRequestBuilder<T> get(final String uri) {
        request.uri(URI.create(uri)).GET();
        return this;
    }
    public HttpRequestBuilder<T> delete(final String uri) {
        request.uri(URI.create(uri)).DELETE();
        return this;
    }
    public HttpRequestBuilder<T> header(final String name, final String value) {
        request.header(name, value);
        return this;
    }
    public HttpRequestBuilder<T> head() {
        request.method("HEAD", BodyPublishers.noBody());
        return this;
    }
    public HttpRequestBuilder<T> delete() {
        request.DELETE();
        return this;
    }
    public HttpRequestBuilder<T> get() {
        request.GET();
        return this;
    }
    public HttpRequestBuilder<T> post(final BodyPublisher publisher) {
        request.POST(publisher);
        return this;
    }
    public HttpRequestBuilder<T> post(final String requestBody) {
        request.POST(BodyPublishers.ofString(requestBody));
        return this;
    }
    public HttpRequestBuilder<T> put(final BodyPublisher publisher) {
        request.PUT(publisher);
        return this;
    }

    public HttpRequestBuilder<T> expectStatusCode(final int code) {
        return expect(response -> {
            if (response.statusCode() != code)
                throw new IOException("Wrong status code. Expected " + code + ", but was " + response.statusCode());
        });
    }
    public HttpRequestBuilder<T> expectSuccess() {
        return expect(response -> {
            if (response.statusCode() < 200 || response.statusCode() > 299)
                throw new IOException("Wrong status code. Expected 2XX, but was " + response.statusCode());
        });
    }
    public HttpRequestBuilder<T> expectSuccess(final Supplier<IOException> supplier) {
        return expect(response -> {
            if (response.statusCode() < 200 || response.statusCode() > 299)
                throw supplier.get();
        });
    }

    public HttpRequestBuilder<T> expect(final Expect<T> expect) {
        if (expects == null) expects = new ArrayList<>();
        expects.add(expect);
        return this;
    }

    public HttpResponse<T> send() throws IOException {
        try {
            final HttpResponse<T> response = http.send(request.build(), handler);
            if (expects != null) {
                for (final var expect : expects) {
                    expect.verify(response);
                }
            }
            return response;
        } catch (InterruptedException e) {
            throw new IOException("Interrupted during IO", e);
        }
    }

    public void execute() throws IOException {
        send();
    }

}
