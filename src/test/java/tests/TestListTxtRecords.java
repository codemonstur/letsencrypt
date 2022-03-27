package tests;

import org.xbill.DNS.TextParseException;

import java.net.UnknownHostException;

import static letsencrypt.core.GoDaddy.listTxtRecords;
import static letsencrypt.core.GoDaddy.nameservers;
import static tests.TestShortName.toAcmeChallengeName;
import static tests.util.Secrets.secret;

public class TestListTxtRecords {

    public static void main(final String... args) throws TextParseException, UnknownHostException {
        final var domain = secret("DOMAIN");
        final var hostname = secret("HOSTNAME");

        final var records = listTxtRecords(nameservers(domain), toAcmeChallengeName(domain, hostname) + "." + domain);

        System.out.println(records);
    }

}
