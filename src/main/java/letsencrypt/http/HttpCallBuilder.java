package letsencrypt.http;

import com.google.gson.Gson;
import letsencrypt.util.Functions;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Arrays.stream;

public final class HttpCallBuilder implements ProtocolBuilder, MethodUriBuilder, HeaderBodyBuilder, ExpectBuilder, RequestExecutor {

    public static MethodUriBuilder newHttpCall(final String hostname) {
        return new HttpCallBuilder().scheme("http").port(80).hostname(hostname);
    }
    public static MethodUriBuilder newHttpsCall(final String hostname) {
        return new HttpCallBuilder().scheme("https").port(443).hostname(hostname);
    }
    public static MethodUriBuilder newHttpCall(final String hostname, final int port) {
        return new HttpCallBuilder().scheme("http").port(port).hostname(hostname);
    }
    public static MethodUriBuilder newHttpsCall(final String hostname, final int port) {
        return new HttpCallBuilder().scheme("https").port(port).hostname(hostname);
    }

    private String scheme;
    private String hostname;
    private int port;
    private String method;
    private String uri;
    private Map<String, String> headers;
    private BodyPublisher body;
    private List<Expect<String>> expects;

    public ProtocolBuilder scheme(final String scheme) {
        this.scheme = scheme;
        return this;
    }
    public MethodUriBuilder hostname(final String hostname) {
        this.hostname = hostname;
        return this;
    }
    public ProtocolBuilder port(final int port) {
        this.port = port;
        return this;
    }

    public HeaderBodyBuilder head(final String uri) {
        return method("HEAD", uri);
    }
    public HeaderBodyBuilder head(final String uriFormat, final Object... arguments) {
        return head(String.format(uriFormat, toUrlSafeArguments(arguments)));
    }
    public HeaderBodyBuilder get(final String uri) {
        return method("GET", uri);
    }
    public HeaderBodyBuilder get(final String uriFormat, final Object... arguments) {
        return get(String.format(uriFormat, toUrlSafeArguments(arguments)));
    }
    public HeaderBodyBuilder post(final String uri) {
        return method("POST", uri);
    }
    public HeaderBodyBuilder post(final String uriFormat, final Object... arguments) {
        return post(String.format(uriFormat, toUrlSafeArguments(arguments)));
    }
    public HeaderBodyBuilder put(final String uri) {
        return method("PUT", uri);
    }
    public HeaderBodyBuilder put(final String uriFormat, final Object... arguments) {
        return put(String.format(uriFormat, toUrlSafeArguments(arguments)));
    }
    public HeaderBodyBuilder delete(final String uri) {
        return method("DELETE", uri);
    }
    public HeaderBodyBuilder delete(final String uriFormat, final Object... arguments) {
        return delete(String.format(uriFormat, toUrlSafeArguments(arguments)));
    }
    public HeaderBodyBuilder method(final String method, final String uri) {
        this.method = method;
        this.uri = uri;
        return this;
    }
    public HeaderBodyBuilder method(final String method, final String uriFormat, final Object... arguments) {
        return method(method, String.format(uriFormat, toUrlSafeArguments(arguments)));
    }

    private static Object[] toUrlSafeArguments(final Object[] objects) {
        return stream(objects)
            .map(Object::toString)
            .map(Functions::urlEncode)
            .toArray();
    }

    public HeaderBodyBuilder header(final String name, final String value) {
        if (headers == null) headers = new HashMap<>();
        headers.put(name, value);
        return this;
    }

    public ExpectBuilder body(final String body) {
        this.body = BodyPublishers.ofString(body);
        return this;
    }
    public ExpectBuilder body(final BodyPublisher body) {
        this.body = body;
        return this;
    }
    public ExpectBuilder jsonBody(final Object body) {
        this.body = BodyPublishers.ofString(GSON.toJson(body));
        return this;
    }


    public HttpCallBuilder expectStatusCode(final int code) {
        return expect(response -> {
            if (response.statusCode() != code)
                throw new IOException("Wrong status code. Expected " + code + ", but was " + response.statusCode());
        });
    }
    public HttpCallBuilder expectSuccess() {
        return expect(response -> {
            if (response.statusCode() < 200 || response.statusCode() > 299)
                throw new IOException("Wrong status code. Expected 2XX, but was " + response.statusCode());
        });
    }
    public HttpCallBuilder expectSuccess(final Supplier<? extends IOException> supplier) {
        return expect(response -> {
            if (response.statusCode() < 200 || response.statusCode() > 299)
                throw supplier.get();
        });
    }

    public HttpCallBuilder expect(final Expect<String> expect) {
        if (expects == null) expects = new ArrayList<>();
        expects.add(expect);
        return this;
    }


    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    public <T> T fetchInto(final Type type) throws IOException {
        try {
            final var response = verify(response(buildRequest()));
            return GSON.fromJson(response.body(), type);
        } catch (InterruptedException e) {
            throw new IOException("Interrupted during IO", e);
        }
    }
    public <T> T fetchInto(final Class<T> clazz) throws IOException {
        try {
            final var response = verify(response(buildRequest()));
            return GSON.fromJson(response.body(), clazz);
        } catch (InterruptedException e) {
            throw new IOException("Interrupted during IO", e);
        }
    }

    public void execute() throws IOException {
        try {
            verify(response(buildRequest()));
        } catch (InterruptedException e) {
            throw new IOException("Interrupted during IO", e);
        }
    }

    private static HttpResponse<String> response(final HttpRequest request) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(request, BodyHandlers.ofString());
    }
    private HttpResponse<String> verify(final HttpResponse<String> response) throws IOException {
        if (expects == null) return response;
        for (final var expect : expects) {
            expect.verify(response);
        }

        return response;
    }

    private HttpRequest buildRequest() {
        final var request = HttpRequest.newBuilder()
            .uri(URI.create(scheme + "://" + hostname + ":" + port + uri))
            .method(method, body == null ? BodyPublishers.noBody() : body);
        if (headers != null) for (final var header : headers.entrySet())
            request.header(header.getKey(), header.getValue());
        return request.build();
    }

}
