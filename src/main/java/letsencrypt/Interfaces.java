package letsencrypt;

import com.google.gson.Gson;
import letsencrypt.dto.DnsRecord;
import org.shredzone.acme4j.exception.AcmeException;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.List;

public enum Interfaces {;

    public interface AccessGson {
        Gson getGson();
        default String toJson(final Object o) {
            return getGson().toJson(o);
        }
    }

    public interface Authorizer {
        void performAuth(String domain, List<DnsRecord> records) throws IOException, AcmeException, InterruptedException, NamingException;
    }

}
