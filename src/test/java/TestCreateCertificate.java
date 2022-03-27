import jcli.errors.InvalidCommandLine;
import org.shredzone.acme4j.exception.AcmeException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static letsencrypt.actions.CreateAccount.createAccount;
import static letsencrypt.actions.CreateCertificate.createCertificate;
import static letsencrypt.actions.CreateKeyPair.createKeyPair;

public class TestCreateCertificate {

    public static void main(final String... args)
            throws AcmeException, IOException, InterruptedException, InvalidCommandLine, TimeoutException {
        createKeyPair(null, null, new String[] {"-o", "letsencrypt/target/account.pem"});
        createAccount(null, null, new String[] {
            "--staging", "-q", "-k", "letsencrypt/target/account.pem", "--email", "test@test.com"
        });
        createKeyPair(null, null, new String[] {"-o", "letsencrypt/target/domain.pem"});
        createCertificate(null, null, new String[] {
            "--staging",
            "--organization", "the-org",
            "--api-key", "the-godaddy-key",
            "-h", "the-full-hostname",
            "-d", "the-root-domain",
            "-a", "letsencrypt/target/account.pem",
            "-k", "letsencrypt/target/domain.pem",
            "-o", "letsencrypt/target/cert.crt"
        });
    }

}
