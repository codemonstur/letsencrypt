package letsencrypt.actions;

import bobthebuildtool.pojos.buildfile.Project;
import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;

import java.io.IOException;
import java.util.Map;

import static jcli.CliParserBuilder.newCliParser;
import static letsencrypt.util.Crypto.newKeypair;
import static letsencrypt.util.Crypto.saveKeyPair;

public enum CreateKeyPair {;

    @CliCommand(name = "create-keypair", description = "create a cryptographic keypair")
    public static final class CliArguments {
        @CliOption(name = 'o', longName = "output-file", isMandatory = true, description = "The path to write the keypair to in PEM format")
        public String outputFile;
        @CliOption(name = 'h', longName = "help", isHelp = true)
        public boolean help;
    }

    public static int createKeyPair(final Project project, final Map<String, String> env, final String[] args)
            throws IOException, InvalidCommandLine {
        final CliArguments arguments = newCliParser(CliArguments::new).parse(args);

        saveKeyPair(newKeypair(), arguments.outputFile);
        return 0;
    }

}