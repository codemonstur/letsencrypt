import letsencrypt.dto.CliArguments;
import org.shredzone.acme4j.exception.AcmeException;

import java.io.IOException;

import static letsencrypt.actions.CreateAccount.createAccount;
import static letsencrypt.actions.CreateKeyPair.createKeyPair;

public class TestCreateAccount {

    public static void main(final String... args) throws AcmeException, IOException {
        final var key = new CliArguments();
        key.outputFile = "letsencrypt/target/key.pem";
        createKeyPair(key);

        final var account = new CliArguments();
        account.staging = true;
        account.accountKeyPairFile = "letsencrypt/target/key.pem";
        account.emailAddress = "jegvoorneveld@gmail.com";
        createAccount(account);
    }

}
