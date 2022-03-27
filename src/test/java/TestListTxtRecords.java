import org.xbill.DNS.TextParseException;

import java.net.UnknownHostException;

import static letsencrypt.core.GoDaddy.listTxtRecords;
import static letsencrypt.core.GoDaddy.nameservers;

public class TestListTxtRecords {

    public static void main(final String... args) throws TextParseException, UnknownHostException {
        System.out.println(listTxtRecords(nameservers("the-root-domain"), "_acme-challenge.the-root-domain"));
    }

}
