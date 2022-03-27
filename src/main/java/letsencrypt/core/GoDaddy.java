package letsencrypt.core;

import letsencrypt.util.DnsRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static bobthebuildtool.services.Functions.isNullOrEmpty;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static letsencrypt.http.HttpCallBuilder.newHttpsCall;
import static letsencrypt.util.DnsRecord.TYPE_LIST_RECORDS;
import static letsencrypt.util.Functions.isInThePast;
import static org.xbill.DNS.Type.NS;
import static org.xbill.DNS.Type.TXT;

public enum GoDaddy {;

    public static List<DnsRecord> getRecords(final String apiKey, final String domain) throws IOException {
        return newHttpsCall("api.godaddy.com")
            .get("/v1/domains/%s/records", domain)
            .header("Authorization", "sso-key " + apiKey)
            .expectSuccess()
            .fetchInto(TYPE_LIST_RECORDS);
    }

    public static void setRecord(final String apiKey, final String domain, final String type, final String name, final String value) throws IOException {
        setRecord(apiKey, domain, type, name, singletonList(new DnsRecord(type, name, value)));
    }

    public static void setRecord(final String apiKey, final String domain, final String type, final String name, final List<DnsRecord> records) throws IOException {
        newHttpsCall("api.godaddy.com")
            .put("/v1/domains/%s/records/%s/%s", domain, type, name)
            .header("Authorization", "sso-key " + apiKey)
            .header("Content-Type", "application/json")
            .jsonBody(records)
            .expectSuccess()
            .execute();
    }

    public static void deleteRecord(final String apiKey, final String domain, final String type, final String name) throws IOException {
        newHttpsCall("api.godaddy.com")
            .put("/v1/domains/%s/records/%s/%s", domain, type, name)
            .header("Authorization", "sso-key " + apiKey)
            .header("Content-Type", "application/json")
            .jsonBody(List.of(new DnsRecord(type, name, "")))
            .expectSuccess()
            .execute();
    }

    // Tried really hard to get this to work. DNS has too many caching tricks and just gets in the way.
    // If you try to query like this you will always and forever get the wrong value. Even after the
    // whole world already gets the right value. It will not pass until the 10-minute timeout is over.
    //
    // So we are better off just waiting 10 minutes.
    public static void waitForGoDaddyUpdate(final String domain, final String name, final String expected, final long maxWait)
            throws TextParseException, UnknownHostException, InterruptedException, TimeoutException {
        final var hostname = name + "." + domain;
        final var nameservers = nameservers(domain);
        final long stopTime = currentTimeMillis() + maxWait;

        while (true) {
            if (isInThePast(stopTime)) throw new TimeoutException("GoDaddy did not make the correct value available in time");
            if (allMatch(listTxtRecords(nameservers, hostname), expected)) break;
            //noinspection BusyWait
            Thread.sleep(5_000);
        }
    }

    public static List<String> nameservers(final String domain) throws TextParseException {
        final var lookup = new Lookup(domain, NS).run();
        if (lookup == null) return Collections.emptyList();
        return stream(lookup).map(Record::getAdditionalName).map(Name::toString).collect(toList());
    }

    public static List<String> listTxtRecords(final List<String> nameservers, final String hostname)
            throws TextParseException, UnknownHostException {
        final List<String> records = new ArrayList<>();

        // GoDaddy has two nameservers, and they sometimes disagree with each other. So we need to find
        // both and query both.
        for (final var nameserver : nameservers) {
            final var lookup = new Lookup(hostname, TXT);
            lookup.setResolver(new SimpleResolver(nameserver));
            final var dnsTypeLookup = lookup.run();
            if (dnsTypeLookup == null) continue;
            // We add all records there might be, but there really should be only one here
            for (final var record : dnsTypeLookup) {
                records.addAll(((TXTRecord) record).getStrings());
            }
        }

        return records;
    }

    private static boolean allMatch(final List<String> list, final String expected) {
        if (isNullOrEmpty(list)) return false;
        for (final var value : list) if (!expected.equals(value)) return false;

        return true;
    }

}
