package letsencrypt.http;

public interface ProtocolBuilder {
    ProtocolBuilder scheme(String scheme);
    ProtocolBuilder port(int port);
    MethodUriBuilder hostname(String hostname);
}
