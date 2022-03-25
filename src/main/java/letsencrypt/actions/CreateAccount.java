package letsencrypt.actions;

import letsencrypt.dto.CliArguments;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeException;

import java.io.IOException;
import java.security.KeyPair;

import static bobthebuildtool.services.Log.logInfo;
import static letsencrypt.util.Crypto.loadKeyPair;

public enum CreateAccount {;

    public static int createAccount(final CliArguments arguments) throws AcmeException, IOException {
        logInfo("Loading account keypair at " + arguments.accountKeyPairFile);
        final KeyPair accountKeyPair = loadKeyPair(arguments.accountKeyPairFile);
        final String serverUri = arguments.staging ? "acme://letsencrypt.org/staging" : "acme://letsencrypt.org";
        logInfo("Opening session to " + serverUri);
        final Session session = new Session(serverUri);
        logInfo("Creating account for " + arguments.emailAddress);
        newAccount(session, accountKeyPair, arguments.emailAddress);
        return 0;
    }

    private static Account newAccount(final Session session, final KeyPair keyPair, final String email) throws AcmeException {
        return new AccountBuilder()
            .addContact("mailto:" + email)
            .agreeToTermsOfService()
            .useKeyPair(keyPair)
            .create(session);
    }

}
