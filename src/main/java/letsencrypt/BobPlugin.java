package letsencrypt;

import bobthebuildtool.pojos.buildfile.Project;
import bobthebuildtool.pojos.error.VersionTooOld;
import bobthebuildtool.services.commands.Command;
import letsencrypt.actions.CreateAccount;
import letsencrypt.actions.CreateCertificate;
import letsencrypt.actions.CreateKeyPair;
import letsencrypt.actions.ListGoDaddyRecords;

import static bobthebuildtool.services.Update.requireBobVersion;

public enum BobPlugin {;

    private static final String
        DESCRIPTION_ACCOUNT = "Create a new account at letsencrypt using a given keypair",
        DESCRIPTION_CERTIFICATE = "Creates a certificate using LetsEncrypt and GoDaddy",
        DESCRIPTION_KEYPAIR = "Create a keypair for use in LetsEncrypt accounts or certificates",
        DESCRIPTION_LIST_DNS = "List DNS records in GoDaddy";

    public static void installPlugin(final Project project) throws VersionTooOld {
        requireBobVersion("7");

        final var classLoader = Thread.currentThread().getContextClassLoader();

        project.addCommand("le-account", DESCRIPTION_ACCOUNT,
                setClassLoaderFirst(classLoader, CreateAccount::createAccount));
        project.addCommand("le-certificate", DESCRIPTION_CERTIFICATE,
                setClassLoaderFirst(classLoader, CreateCertificate::createCertificate));
        project.addCommand("create-keypair", DESCRIPTION_KEYPAIR, CreateKeyPair::createKeyPair);
        project.addCommand("list-godaddy-records", DESCRIPTION_LIST_DNS, ListGoDaddyRecords::listRecords);
    }

    // Acme4j uses a ServiceLoader, this class needs the context class loader set
    private static Command setClassLoaderFirst(final ClassLoader classLoader, final Command command) {
        return (project, environment, args) -> {
            final var original = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            try {
                return command.execute(project, environment, args);
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
        };
    }

}
