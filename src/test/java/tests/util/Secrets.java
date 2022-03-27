package tests.util;

import java.util.Properties;

public enum Secrets {;

    private static final Properties secrets = loadSecretProperties();

    private static Properties loadSecretProperties() {
        final var props = new Properties();
        try (final var in = Secrets.class.getResourceAsStream("/.secrets")) {
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Missing .secrets file in test resources");
        }
        return props;
    }

    public static String secret(final String name) {
        return secrets.getProperty(name);
    }

}
