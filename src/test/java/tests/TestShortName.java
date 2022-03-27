package tests;

public class TestShortName {

    public static void main(final String... args) {
        check("*.*.something.domain.tld", "_acme-challenge.*.something");
        check("*.something.domain.tld", "_acme-challenge.something");
        check("*.domain.tld", "_acme-challenge");
        check("something.domain.tld", "_acme-challenge.something");
        check("something.something.domain.tld", "_acme-challenge.something.something");
    }

    private static void check(final String input, final String expected) {
        final var output = toAcmeChallengeName("domain.tld", input);
        if (!output.equals(expected))
            System.out.println("Failed input " + input + ", output was " + output + ", but should be " + expected);
    }

    public static String toAcmeChallengeName(final String domain, final String hostname) {
        final int offsetDomain = hostname.length() - (domain.length() + 1);
        if (hostname.startsWith("*.")) {
            final var dotOffset = hostname.indexOf('.');
            final var shortName = hostname.substring(dotOffset, offsetDomain);
            return "_acme-challenge" + (".".equals(shortName) ? "" : shortName);
        } else {
            final var shortName = hostname.substring(0, offsetDomain);
            return "_acme-challenge." + shortName;
        }
    }

}
