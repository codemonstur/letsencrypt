public class TestShortName {

    public static void main(final String... args) {
        check("*.*.something.domain.tld", "_acme-challenge.*.something");
        check("*.something.domain.tld", "_acme-challenge.something");
        check("*.domain.tld", "_acme-challenge");
        check("something.domain.tld", "_acme-challenge");
    }

    private static void check(final String input, final String expected) {
        final var output = toAcmeChallengeName("domain.tld", input);
        if (!output.equals(expected))
            System.out.println("Failed input " + input + ", output was " + output + ", but should be " + expected);
    }

    private static String toAcmeChallengeName(final String domain, final String hostname) {
        final var dotOffset = hostname.indexOf('.');
        final var shortName = hostname.substring(dotOffset, hostname.length() - (domain.length() + 1));
        return "_acme-challenge" + (".".equals(shortName) ? "" : shortName);
    }

}
