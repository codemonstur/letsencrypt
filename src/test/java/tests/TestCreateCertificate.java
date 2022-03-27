package tests;

import static letsencrypt.actions.CreateAccount.createAccount;
import static letsencrypt.actions.CreateCertificate.createCertificate;
import static letsencrypt.actions.CreateKeyPair.createKeyPair;
import static tests.util.Secrets.secret;

public class TestCreateCertificate {

    public static void main(final String... args) throws Exception {
        setup();
//        certificate(secret("WILD_HOSTNAME"));
        certificate(secret("ROOT_HOSTNAME"));
    }

    private static void setup() throws Exception {
        createKeyPair(null, null, new String[] {"-o", "letsencrypt/target/account.pem"});
        createAccount(null, null, new String[] {
            "--staging", "-q", "-k", "letsencrypt/target/account.pem", "--email", secret("EMAIL_ADDRESS")
        });
        createKeyPair(null, null, new String[] {"-o", "letsencrypt/target/domain.pem"});
    }
    private static void certificate(final String hostname) throws Exception {
        createCertificate(null, null, new String[] {
            "--staging",
            "--organization", secret("ORGANIZATION"),
            "--api-key", secret("GODADDY_API_KEY"),
            "-h", hostname,
            "-d", secret("DOMAIN"),
            "-a", "letsencrypt/target/account.pem",
            "-k", "letsencrypt/target/domain.pem",
            "-o", "letsencrypt/target/cert.crt"
        });
    }
}
