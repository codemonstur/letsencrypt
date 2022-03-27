package letsencrypt.actions;

import bobthebuildtool.pojos.buildfile.Project;
import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeException;

import java.io.IOException;
import java.util.Map;

import static jcli.CliParserBuilder.newCliParser;
import static letsencrypt.core.LetsEncrypt.newLetsEncryptAccount;
import static letsencrypt.util.Crypto.loadKeyPair;
import static letsencrypt.util.Functions.logInfo;

public enum CreateAccount {;

    @CliCommand(name = "letsencrypt-account", description = "create a cryptographic keypair")
    public static final class CliArguments {
        @CliOption(longName = "staging", description = "Flag to use the staging server URI instead of the production URI")
        public boolean staging;

        @CliOption(name = 'k', longName = "keypair", isMandatory = true, description = "Path to a PEM file to use for the new account")
        public String keyPairFile;
        @CliOption(name = 'e', longName = "email", isMandatory = true, description = "The email address to connect the account to")
        public String emailAddress;

        @CliOption(name = 'q', longName = "quiet", description = "Print no output")
        public boolean quiet;

        @CliOption(name = 'h', longName = "help", isHelp = true)
        public boolean help;
    }

    public static int createAccount(final Project project, final Map<String, String> env, final String[] args)
            throws AcmeException, IOException, InvalidCommandLine {
        final CliArguments arguments = newCliParser(CliArguments::new).parse(args);

        final var logEnabled = !arguments.quiet;

        logInfo(logEnabled, "Loading account keypair at " + arguments.keyPairFile);
        final var accountKeyPair = loadKeyPair(arguments.keyPairFile);

        final var serverUri = arguments.staging ? "acme://letsencrypt.org/staging" : "acme://letsencrypt.org";
        logInfo(logEnabled, "Connecting to " + serverUri);
        final var session = new Session(serverUri);

        logInfo(logEnabled, "Creating account for " + arguments.emailAddress);
        newLetsEncryptAccount(session, accountKeyPair, arguments.emailAddress);
        return 0;
    }

}
