package letsencrypt.util;

import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;

import static org.shredzone.acme4j.util.KeyPairUtils.readKeyPair;
import static org.shredzone.acme4j.util.KeyPairUtils.writeKeyPair;

public enum Crypto {;

    public static KeyPair newKeypair() {
        return KeyPairUtils.createKeyPair(2048);
    }

    public static KeyPair loadKeyPair() throws IOException {
        return loadKeyPair("keypair.pem");
    }
    public static KeyPair loadKeyPair(final String filename) throws IOException {
        try (final var reader = new FileReader(filename)) {
            return readKeyPair(reader);
        }
    }

    public static void saveKeyPair(final KeyPair keyPair) throws IOException {
        saveKeyPair(keyPair, "keypair.pem");
    }
    public static void saveKeyPair(final KeyPair keyPair, final String filename) throws IOException {
        try (final var writer = new FileWriter(filename)) {
            writeKeyPair(keyPair, writer);
        }
    }

    public static void writeCertificate(final Certificate cert) throws IOException {
        writeCertificate(cert, "cert-chain.crt");
    }
    public static void writeCertificate(final Certificate cert, final String filename) throws IOException {
        try (final var writer = new FileWriter(filename)) {
            cert.writeCertificate(writer);
        }
    }

}
