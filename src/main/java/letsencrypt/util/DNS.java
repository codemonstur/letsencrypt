package letsencrypt.util;

import org.xbill.DNS.*;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public enum DNS {;

    public static List<String> queryDns(final int type, final String domain, final String name) throws TextParseException, UnknownHostException {
        final Lookup lookup = new Lookup(name+"."+domain, type);
        lookup.setResolver(new SimpleResolver(getFirstNameserver(domain)));

        for (final org.xbill.DNS.Record record : lookup.run()) {
            TXTRecord txt = (TXTRecord) record;
            return txt.getStrings();
        }
        return Collections.emptyList();
    }

    private static String getFirstNameserver(final String domain) throws TextParseException {
        final Lookup lookup = new Lookup(domain, Type.NS);
        for (final org.xbill.DNS.Record record : lookup.run()) {
            return record.getAdditionalName().toString();
        }
        return null;
    }

}
