package letsencrypt.dao;

import letsencrypt.Interfaces.AccessGson;
import letsencrypt.dto.DnsRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import javax.naming.NamingException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static bobthebuildtool.services.Log.logInfo;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static letsencrypt.dto.DnsRecord.TYPE_LIST_RECORDS;
import static letsencrypt.util.DNS.queryDns;
import static letsencrypt.util.GsonHttpRequestBuilder.newJsonHttpRequest;
import static letsencrypt.util.HttpRequestBuilder.newHttpRequest;
import static letsencrypt.util.Validator.urlEncode;

public interface GoDaddy extends AccessGson, AccessHttpClient {

    String getGoDaddyApiKey();

    default List<DnsRecord> getRecords(final String domain) throws IOException {
        return newJsonHttpRequest(getGson(), getHttpClient())
            .uri(format("https://api.godaddy.com/v1/domains/%s/records", urlEncode(domain)))
            .header("Authorization", "sso-key " + getGoDaddyApiKey())
            .get()
            .expectSuccess()
            .fetchInto(TYPE_LIST_RECORDS);
    }

    default void setRecord(final String domain, final String type, final String name, final String value) throws IOException {
        setRecord(domain, type, name, singletonList(new DnsRecord(type, name, value)));
    }

    default void setRecord(final String domain, final String type, final String name, final List<DnsRecord> records) throws IOException {
        final var response = newHttpRequest()
            .uri(format("https://api.godaddy.com/v1/domains/%s/records/%s/%s", urlEncode(domain), urlEncode(type), urlEncode(name)))
            .header("Authorization", "sso-key " + getGoDaddyApiKey())
            .header("Content-Type", "application/json")
            .put(ofString(toJson(records)))
            .send();
        if (!isSuccessful(response)) {
            System.out.println(response.body());
            throw new IOException("Wrong status code " + response.statusCode());
        }
    }

    default void deleteRecord(final String domain, final String type, final String name) throws IOException {
        final List<DnsRecord> records = Arrays.asList(new DnsRecord(type, name, ""));

        final var response = newHttpRequest()
            .uri(format("https://api.godaddy.com/v1/domains/%s/records/%s/%s", urlEncode(domain), urlEncode(type), urlEncode(name)))
            .header("Authorization", "sso-key " + getGoDaddyApiKey())
            .header("Content-Type", "application/json")
            .put(ofString(toJson(records)))
            .send();
        if (!isSuccessful(response)) {
            throw new IOException("Wrong status code " + response.statusCode() + ", body:\n" + response.body());
        }
    }

    private static boolean isSuccessful(final HttpResponse<?> response) {
        return response.statusCode() >= 200 && response.statusCode() <= 299;
    }

    default void authorizeDns(final String domain, final List<DnsRecord> records) throws IOException,
            InterruptedException, NamingException {
        logInfo("Updating GoDaddy records for domain " + domain);
        deleteRecord(domain, "TXT", "_acme-challenge");
        setRecord(domain, "TXT", "_acme-challenge", records);
        for (final var record : records) {
            waitForGoDaddyUpdate(domain, "_acme-challenge", record.data, MINUTES.toMillis(5));
        }
    }

    private static void waitForGoDaddyUpdate(final String domain, final String name, final String expected, final long maxWait)
            throws NamingException, InterruptedException, TextParseException, UnknownHostException {
        logInfo("Waiting for GoDaddy TXT record for " + domain + " to become " + expected);
        final long stopAt = currentTimeMillis() + maxWait;
        boolean dnsHasValue = false;
        while (!dnsHasValue) {
            if (stopAt < currentTimeMillis()) throw new NamingException("GoDaddy did not make the correct value available in time");

            Thread.sleep(3000L);
            final List records = queryDns(Type.TXT, domain, name);

            if (records.isEmpty()) logInfo("Currently no record of type TXT with name " + domain + " can be found");

            for (final var record : records) {
                logInfo("Value of TXT record for " + domain + " is " + record);
                dnsHasValue = dnsHasValue || record.equals(expected);
            }
        }
    }

}
