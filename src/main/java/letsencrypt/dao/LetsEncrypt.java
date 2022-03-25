package letsencrypt.dao;

import letsencrypt.Interfaces.Authorizer;
import letsencrypt.dto.DnsRecord;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;

import javax.naming.NamingException;
import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bobthebuildtool.services.Log.logInfo;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.shredzone.acme4j.Status.VALID;

public interface LetsEncrypt {

    KeyPair getAccountKeyPair();
    KeyPair getDomainKeyPair();

    default Account loadAccount(final Session session) throws AcmeException {
        return new AccountBuilder()
            .onlyExisting()
            .useKeyPair(getAccountKeyPair())
            .create(session);
    }

    default Certificate newCertificateForDomains(final Account account, final Authorizer authoriser, final List<String> domains)
            throws AcmeException, InterruptedException, IOException, NamingException {
        final Order order =  account.newOrder().domains(domains).create();
        return executeOrder(performChallenges(order, authoriser), domains).getCertificate();
    }

    private Order executeOrder(final Order order, final List<String> domains)
            throws IOException, AcmeException, InterruptedException {
        logInfo("Creating new CSR");
        final byte[] csr = newCSR(getDomainKeyPair(), domains);
        order.execute(csr);
        waitForOrderAccepted(order, MINUTES.toMillis(5));
        return order;
    }

    private static byte[] newCSR(final KeyPair domainKeyPair, final List<String> domains) throws IOException {
        final CSRBuilder csr = new CSRBuilder();
        csr.addDomains(domains);
        csr.setOrganization("3rd-stage");
        csr.sign(domainKeyPair);
        return csr.getEncoded();
    }

    private static Order performChallenges(final Order order, final Authorizer authoriser) throws IOException, AcmeException, InterruptedException, NamingException {
        final Map<String, List<DnsRecord>> map = new HashMap<>();
        for (final var auth : order.getAuthorizations()) {
            if (auth.getStatus() != VALID) {
                final Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE);
                if (challenge != null) {
                    map.computeIfAbsent(auth.getIdentifier().getDomain(),
                        s -> new ArrayList<>()).add(new DnsRecord("TXT", "_acme-challenge", challenge.getDigest()));
                }
            }
        }

        for (final var entry : map.entrySet()) {
            logInfo("Performing challenge for " + entry.getKey());
            authoriser.performAuth(entry.getKey(), entry.getValue());
        }

        for (final var auth : order.getAuthorizations()) {
            if (auth.getStatus() != VALID) {
                final Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE);
                if (challenge != null) {
                    challenge.trigger();
                    waitForChallengeAccepted(auth, MINUTES.toMillis(5));
                }
            }
        }

        return order;
    }

    private static void waitForChallengeAccepted(final Authorization auth, final long maxWait) throws InterruptedException, AcmeException {
        final long stopAt = currentTimeMillis() + maxWait;
        Status status = auth.getStatus();
        while (status != Status.VALID) {
            logInfo("Current status of challenge is " + status);
            if (stopAt < currentTimeMillis()) throw new AcmeException("Did not receive a valid status in time");

            Thread.sleep(3000L);
            auth.update();
            status = auth.getStatus();
        }
    }

    private static void waitForOrderAccepted(final Order order, final long maxWait) throws InterruptedException, AcmeException {
        logInfo("Waiting for order to be accepted");
        final long stopAt = currentTimeMillis() + maxWait;
        Status status = order.getStatus();
        while (status != VALID) {
            logInfo("Current status of order is " + status);
            if (stopAt < currentTimeMillis()) throw new AcmeException("Did not receive a valid status in time");

            Thread.sleep(3000L);
            order.update();
            status = order.getStatus();
        }
    }

}
