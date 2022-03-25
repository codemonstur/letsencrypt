package letsencrypt.actions;

import letsencrypt.dto.CliArguments;

import java.io.IOException;

import static letsencrypt.util.Crypto.newKeypair;
import static letsencrypt.util.Crypto.saveKeyPair;

public enum CreateKeyPair {;

    public static int createKeyPair(final CliArguments arguments) throws IOException {
        saveKeyPair(newKeypair(), arguments.outputFile);
        return 0;
    }

}