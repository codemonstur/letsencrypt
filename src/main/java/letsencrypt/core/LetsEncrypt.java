package letsencrypt.core;

import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;

import java.io.IOException;
import java.security.KeyPair;
import java.util.concurrent.TimeoutException;

import static java.lang.System.currentTimeMillis;
import static letsencrypt.util.Exceptions.*;
import static letsencrypt.util.Functions.isInThePast;
import static org.shredzone.acme4j.Status.*;

public enum LetsEncrypt {;

    public static Account newLetsEncryptAccount(final Session session, final KeyPair keyPair, final String email) throws AcmeException {
        return new AccountBuilder()
            .addContact("mailto:" + email)
            .agreeToTermsOfService()
            .useKeyPair(keyPair)
            .create(session);
    }

    public static Account loadLetsEncryptAccount(final KeyPair accountKeyPair, final Session session) throws AcmeException {
        return new AccountBuilder()
            .onlyExisting()
            .useKeyPair(accountKeyPair)
            .create(session);
    }

    public static String getDNSChallengeDigest(final Order order) throws AcmeException {
        return findChallenge(findAuthorization(order)).getDigest();
    }

    public static void executeChallengeAndWait(final Order order, final long maxWaitTime) throws AcmeException, InterruptedException, TimeoutException {
        final var auth = findAuthorization(order);
        findChallenge(auth).trigger();
        waitForChallengeAccepted(auth, maxWaitTime);
    }

    private static Authorization findAuthorization(final Order order) {
        final var authorizations = order.getAuthorizations();
        if (authorizations.isEmpty()) throw new IllegalArgumentException("LE order contains no authorizations");
        if (authorizations.size() != 1) throw new IllegalArgumentException("Too many LE authorizations, should be only 1");
        return authorizations.get(0);
    }
    private static Dns01Challenge findChallenge(final Authorization auth) throws AcmeException {
        if (auth.getStatus() != PENDING) throw challengeFailedBeforeTried(auth);
        final Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE);
        if (challenge == null) throw new IllegalArgumentException("Missing DNS challenge for LE Authorization");
        return challenge;
    }

    public static void waitForChallengeAccepted(final Authorization auth, final long maxWaitTime)
            throws InterruptedException, AcmeException, TimeoutException {
        final long stopTime = currentTimeMillis() + maxWaitTime;
        while (true) {
            final var status = auth.getStatus();
            if (status == VALID) break;
            if (status == INVALID) throw challengeFailed(auth);
            if (isInThePast(stopTime)) throw new TimeoutException("While waiting for a challenge to complete");

            //noinspection BusyWait
            Thread.sleep(3_000L);
            auth.update();
        }
    }

    public static byte[] newCertificateSigningRequest(final KeyPair domainKeyPair, final String organization
            , final String hostname) throws IOException {
        final CSRBuilder csr = new CSRBuilder();
        csr.addDomain(hostname);
        csr.setOrganization(organization);
        csr.sign(domainKeyPair);
        return csr.getEncoded();
    }

    public static void waitingForOrderCompletion(final Order order, final long maxWait)
            throws InterruptedException, AcmeException, TimeoutException {
        final long stopAt = currentTimeMillis() + maxWait;
        while (true) {
            final var status = order.getStatus();
            if (status == VALID) break;
            if (status == INVALID) throw signingRequestFailed(order);
            if (isInThePast(stopAt)) throw new TimeoutException("While waiting for CSR");

            //noinspection BusyWait
            Thread.sleep(3_000);
            order.update();
        }
    }

}
