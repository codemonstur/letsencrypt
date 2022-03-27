import jcli.errors.InvalidCommandLine;
import org.shredzone.acme4j.exception.AcmeException;

import java.io.IOException;

import static letsencrypt.actions.CreateAccount.createAccount;
import static letsencrypt.actions.CreateKeyPair.createKeyPair;

public class TestCreateAccount {

    public static void main(final String... args) throws AcmeException, IOException, InvalidCommandLine {
        createKeyPair(null, null, new String[] {"-o", "letsencrypt/target/key.pem"});
        createAccount(null, null, new String[] {
            "--staging", "-k", "letsencrypt/target/key.pem", "--email", "test@test.com"
        });
    }

}
