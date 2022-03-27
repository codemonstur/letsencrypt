package letsencrypt.actions;

import bobthebuildtool.pojos.buildfile.Project;
import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import letsencrypt.core.GoDaddy;
import letsencrypt.util.DnsRecord;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static bobthebuildtool.services.Log.logInfo;
import static jcli.CliParserBuilder.newCliParser;

public enum ListGoDaddyRecords {;

    @CliCommand(name = "list-godaddy-records", description = "List DNS records attached to a given hostname in GoDaddy")
    public static final class CliArguments {
        @CliOption(name = 'h', longName = "hostname", isMandatory = true, description = "A hostname to get DNS records for")
        public String hostname;
        @CliOption(longName = "api-key", defaultValue = "", isMandatory = true, description = "The GoDaddy API key in [API_KEY]:[API_SECRET] format")
        public String godaddyApiKey;

        @CliOption(name = 'h', longName = "help", isHelp = true)
        public boolean help;
    }

    public static int listRecords(final Project project, final Map<String, String> env, final String[] args)
            throws IOException, InvalidCommandLine {
        final CliArguments arguments = newCliParser(CliArguments::new).parse(args);

        final List<DnsRecord> records = GoDaddy.getRecords(arguments.godaddyApiKey, arguments.hostname);
        if (records.isEmpty()) return 0;

        logInfo("Records for " + arguments.hostname);
        for (final DnsRecord record : records) {
            logInfo(record.type + "\t- " + record.name + " - " + record.data);
        }

        return 0;
    }

}
