package letsencrypt.util;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class GsonHttpRequestBuilder {

    public static GsonHttpRequestBuilder newJsonHttpRequest(final Gson gson, final HttpClient http) {
        return new GsonHttpRequestBuilder(gson, http);
    }

    private final Gson gson;
    private final HttpClient http;
    private final HttpRequest.Builder request;
    private List<Expect<String>> expects;

    private GsonHttpRequestBuilder(final Gson gson, final HttpClient http) {
        this.http = http;
        this.request = HttpRequest.newBuilder();
        this.gson = gson;
    }

    public GsonHttpRequestBuilder uri(final String uri) {
        return uri(URI.create(uri));
    }
    public GsonHttpRequestBuilder uri(final URI uri) {
        request.uri(uri);
        return this;
    }
    public GsonHttpRequestBuilder get(final String uri) {
        request.uri(URI.create(uri)).GET();
        return this;
    }
    public GsonHttpRequestBuilder header(final String name, final String value) {
        request.header(name, value);
        return this;
    }

    public GsonHttpRequestBuilder get() {
        request.GET();
        return this;
    }
    public GsonHttpRequestBuilder post(final BodyPublisher publisher) {
        request.POST(publisher);
        return this;
    }
    public GsonHttpRequestBuilder post(final String body) {
        request.POST(BodyPublishers.ofString(body));
        return this;
    }
    public GsonHttpRequestBuilder put(final BodyPublisher publisher) {
        request.PUT(publisher);
        return this;
    }

    public <T> T fetchInto(final Type type) throws IOException {
        try {
            final HttpResponse<String> response = http.send(request.build(), BodyHandlers.ofString());
            if (expects != null) {
                for (final var expect : expects) {
                    expect.verify(response);
                }
            }
            return gson.fromJson(response.body(), type);
        } catch (InterruptedException e) {
            throw new IOException("Interrupted during IO", e);
        }
    }
    public <T> T fetchInto(final Class<T> clazz) throws IOException {
        try {
            final HttpResponse<String> response = http.send(request.build(), BodyHandlers.ofString());
            if (expects != null) {
                for (final var expect : expects) {
                    expect.verify(response);
                }
            }
            return gson.fromJson(response.body(), clazz);
        } catch (InterruptedException e) {
            throw new IOException("Interrupted during IO", e);
        }
    }

    public void execute() throws IOException {
        try {
            final HttpResponse<String> response = http.send(request.build(), BodyHandlers.ofString());
            if (expects != null) {
                for (final var expect : expects) {
                    expect.verify(response);
                }
            }
        } catch (InterruptedException e) {
            throw new IOException("Interrupted during IO", e);
        }
    }

    public GsonHttpRequestBuilder expectStatusCode(final int code) {
        return expect(response -> {
            if (response.statusCode() != code)
                throw new IOException("Wrong status code. Expected " + code + ", but was " + response.statusCode());
        });
    }
    public GsonHttpRequestBuilder expectSuccess() {
        return expect(response -> {
            if (response.statusCode() < 200 || response.statusCode() > 299)
                throw new IOException("Wrong status code. Expected 2XX, but was " + response.statusCode());
        });
    }
    public GsonHttpRequestBuilder expectSuccess(final Supplier<? extends IOException> supplier) {
        return expect(response -> {
            if (response.statusCode() < 200 || response.statusCode() > 299)
                throw supplier.get();
        });
    }

    public GsonHttpRequestBuilder expect(final Expect<String> expect) {
        if (expects == null) expects = new ArrayList<>();
        expects.add(expect);
        return this;
    }

}
