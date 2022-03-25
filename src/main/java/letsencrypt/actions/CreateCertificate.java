package letsencrypt.actions;

import com.google.gson.Gson;
import letsencrypt.dao.GoDaddy;
import letsencrypt.dao.LetsEncrypt;
import letsencrypt.dto.CliArguments;
import letsencrypt.util.Crypto;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeException;

import javax.naming.NamingException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.security.KeyPair;

import static bobthebuildtool.services.Log.logInfo;
import static letsencrypt.util.Crypto.loadKeyPair;
import static letsencrypt.util.Validator.requireNotEmpty;

public enum CreateCertificate {;

    public static int createCertificate(final CliArguments arguments) throws AcmeException, IOException, InterruptedException, NamingException {
        requireNotEmpty(arguments.outputFile, "An output file must be provided");
        requireNotEmpty(arguments.domains, "At least one domain must be given");
        requireNotEmpty(arguments.accountKeyPairFile, "The keypair for the account must be given");
        requireNotEmpty(arguments.domainKeyPairFile, "The keypair for the domain must be given");
        requireNotEmpty(arguments.godaddyApiKey, "Please provide an API key and secret in [API-KEY]:[SECRET] format");

        logInfo("Loading account keypair at " + arguments.accountKeyPairFile);
        final KeyPair accountKeyPair = loadKeyPair(arguments.accountKeyPairFile);
        logInfo("Loading domain keypair at " + arguments.domainKeyPairFile);
        final KeyPair domainKeyPair = loadKeyPair(arguments.domainKeyPairFile);

        final HttpClient http = HttpClient.newHttpClient();
        final Gson gson = new Gson();

        final GoDaddy goDaddy = new GoDaddy() {
            public String getGoDaddyApiKey() {
                return arguments.godaddyApiKey;
            }
            public Gson getGson() {
                return gson;
            }
            public HttpClient getHttpClient() {
                return http;
            }
        };
        final LetsEncrypt letsEncrypt = new LetsEncrypt() {
            public KeyPair getAccountKeyPair() {
                return accountKeyPair;
            }
            public KeyPair getDomainKeyPair() {
                return domainKeyPair;
            }
        };

        final String serverUri = arguments.staging ? "acme://letsencrypt.org/staging" : "acme://letsencrypt.org";
        logInfo("Opening session to " + serverUri);
        final Session session = new Session(serverUri);
        final Account account = letsEncrypt.loadAccount(session);
        logInfo("Requesting certificate for domains " + arguments.domains);
        final Certificate certificate = letsEncrypt.newCertificateForDomains(account, goDaddy::authorizeDns, arguments.domains);
        logInfo("Writing certificate to " + arguments.outputFile);
        Crypto.writeCertificate(certificate, arguments.outputFile);

        return 0;
    }

}
