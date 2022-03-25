package letsencrypt.actions;

import com.google.gson.Gson;
import letsencrypt.dao.GoDaddy;
import letsencrypt.dto.CliArguments;
import letsencrypt.dto.DnsRecord;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;

import static letsencrypt.util.Validator.requireNotEmpty;

public enum ListRecords {;

    public static int listRecords(final CliArguments arguments) throws IOException {
        requireNotEmpty(arguments.domains, "Please provide a domain to get records for");
        requireNotEmpty(arguments.godaddyApiKey, "Please provide an API key and secret in [API-KEY]:[SECRET] format");

        final HttpClient http = HttpClient.newHttpClient();
        final Gson gson = new Gson();

        final GoDaddy goDaddy = new GoDaddy() {
            public String getGoDaddyApiKey() {
                return arguments.godaddyApiKey;
            }
            public Gson getGson() {
                return gson;
            }
            public HttpClient getHttpClient() {
                return http;
            }
        };

        for (final String domain : arguments.domains) {
            final List<DnsRecord> records = goDaddy.getRecords(domain);
            if (records.isEmpty()) continue;

            System.out.println("Records for " + domain);
            for (final DnsRecord record : records) {
                System.out.println(record.type + " - " + record.name + " - " + record.data);
            }
        }

        return 0;
    }

}
