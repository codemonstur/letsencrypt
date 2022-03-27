package letsencrypt.actions;

import bobthebuildtool.pojos.buildfile.Project;
import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import letsencrypt.core.GoDaddy;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeException;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MINUTES;
import static jcli.CliParserBuilder.newCliParser;
import static letsencrypt.core.LetsEncrypt.*;
import static letsencrypt.util.Crypto.loadKeyPair;
import static letsencrypt.util.Crypto.writeCertificate;
import static letsencrypt.util.Functions.logInfo;

public enum CreateCertificate {;

    @CliCommand(name = "le-certificate", description = """
        The create-certificate command allows for generating certificates using LetsEncrypt and GoDaddy.
        It will request a DNS TXT record challenge and set the proper value through the GoDaddy API to
        pass the challenge. It is possible to generate both normal and wildcard certificates this way.
        """)
    public static final class CliArguments {
        @CliOption(longName = "staging", description = "Flag to use the staging server URI instead of the production URI")
        public boolean staging;

        @CliOption(longName = "organization", isMandatory = true, description = "The organization name in the certificate")
        public String organization;
        @CliOption(name = 'd', longName = "domain", isMandatory = true, description = "The domain to use for DNS records")
        public String domain;
        @CliOption(name = 'h', longName = "hostname", isMandatory = true, description = "A hostname to request a certificate for")
        public String hostname;
        @CliOption(name = 'k', longName = "domain-keypair-file", isMandatory = true)
        public String domainKeyPairFile;
        @CliOption(name = 'a', longName = "account-keypair-file", isMandatory = true)
        public String accountKeyPairFile;
        @CliOption(name = 'o', longName = "output-file", isMandatory = true)
        public String outputFile;
        @CliOption(longName = "api-key", isMandatory = true, description = "The GoDaddy API key in [API_KEY]:[API_SECRET] format")
        public String godaddyApiKey;

        @CliOption(name = 'q', longName = "quiet", description = "Print no output")
        public boolean quiet;

        @CliOption(longName = "help", isHelp = true)
        public boolean help;
    }

    public static int createCertificate(final Project project, final Map<String, String> env, final String[] args)
            throws AcmeException, IOException, InterruptedException, InvalidCommandLine, TimeoutException {
        final CliArguments arguments = newCliParser(CliArguments::new).parse(args);

        final var logEnabled = !arguments.quiet;

        logInfo(logEnabled, "Loading account and domain key pairs");
        final KeyPair accountKeyPair = loadKeyPair(arguments.accountKeyPairFile);
        final KeyPair domainKeyPair = loadKeyPair(arguments.domainKeyPairFile);

        final var serverUri = arguments.staging ? "acme://letsencrypt.org/staging" : "acme://letsencrypt.org";
        logInfo(logEnabled, "Connecting to " + serverUri);
        final var account = loadLetsEncryptAccount(accountKeyPair, new Session(serverUri));

        logInfo(logEnabled, "Requesting certificate for hostname " + arguments.hostname);
        final var order = account.newOrder().domain(arguments.hostname).create();

        final var challenge = getDNSChallengeDigest(order);
        final var acmeName = toAcmeChallengeName(arguments.domain, arguments.hostname);
        logInfo(logEnabled, "Setting " + acmeName + " TXT record for domain " + arguments.domain + " to " + challenge);
        GoDaddy.deleteRecord(arguments.godaddyApiKey, arguments.domain, "TXT", acmeName);
        GoDaddy.setRecord(arguments.godaddyApiKey, arguments.domain, "TXT", acmeName, challenge);
        logInfo(logEnabled, "TTL in TXT records in GoDaddy is 10 minutes, we will have to wait");
        try {
            Thread.sleep(MINUTES.toMillis(10));

            logInfo(logEnabled, "Triggering challenge at LetsEncrypt and waiting for resolution");
            executeChallengeAndWait(order);

            logInfo(logEnabled, "Executing new Certificate Signing Request");
            order.execute(newCertificateSigningRequest(domainKeyPair, arguments.organization, arguments.hostname));
            logInfo(logEnabled, "Waiting for order to be accepted");
            waitingForOrderCompletion(order, MINUTES.toMillis(5));

            logInfo(logEnabled, "Writing certificate to " + arguments.outputFile);
            writeCertificate(order.getCertificate(), arguments.outputFile);
        } finally {
            logInfo(logEnabled, "Deleting GoDaddy TXT challenge record");
            GoDaddy.deleteRecord(arguments.godaddyApiKey, arguments.domain, "TXT", acmeName);
        }
        return 0;
    }

    // *.*.something.domain.tld -> _acme-challenge.*.something
    // *.something.domain.tld -> _acme-challenge.something
    // *.domain.tld -> _acme-challenge
    // something.domain.tld -> _acme-challenge
    private static String toAcmeChallengeName(final String domain, final String hostname) {
        final var dotOffset = hostname.indexOf('.');
        final var shortName = hostname.substring(dotOffset, hostname.length() - (domain.length() + 1));
        return "_acme-challenge" + (".".equals(shortName) ? "" : shortName);
    }

}
