package letsencrypt;

import bobthebuildtool.pojos.buildfile.Project;
import bobthebuildtool.pojos.error.VersionTooOld;
import jcli.errors.InvalidCommandLine;
import letsencrypt.dto.CliArguments;
import org.shredzone.acme4j.exception.AcmeException;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.Map;

import static bobthebuildtool.services.Update.requireBobVersion;
import static jcli.CliParserBuilder.newCliParser;
import static letsencrypt.actions.CreateAccount.createAccount;
import static letsencrypt.actions.CreateCertificate.createCertificate;
import static letsencrypt.actions.CreateKeyPair.createKeyPair;
import static letsencrypt.actions.ListRecords.listRecords;

public enum BobPlugin {;

    private static final String DESCRIPTION_LETSENCRYPT = "Creating certificates using letsencrypt";

    private static ClassLoader classLoader;

    public static void installPlugin(final Project project) throws VersionTooOld {
        requireBobVersion("7");
        classLoader = Thread.currentThread().getContextClassLoader();
        project.addCommand("letsencrypt", DESCRIPTION_LETSENCRYPT, BobPlugin::letsencrypt);
    }

    private static int letsencrypt(final Project project, final Map<String, String> env, final String[] args)
            throws InvalidCommandLine, AcmeException, IOException, NamingException, InterruptedException {
        final var original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            final CliArguments arguments = newCliParser(CliArguments::new)
                    .onErrorPrintHelpAndExit()
                    .onHelpPrintHelpAndExit()
                    .parse(args);

            return switch (arguments.action) {
                case account -> createAccount(arguments);
                case keypair -> createKeyPair(arguments);
                case certificate -> createCertificate(arguments);
                case records -> listRecords(arguments);
            };
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

}
