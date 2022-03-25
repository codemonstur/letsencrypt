package letsencrypt.dto;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public final class DnsRecord {

    public static final Type TYPE_LIST_RECORDS = new TypeToken<List<DnsRecord>>(){}.getType();

    public final String data;
    public final String name;
    public final Integer port;
    public final Integer priority;
    public final String protocol;
    public final String service;
    public final Integer ttl = 600;
    public final String type;
    public final Integer weight;

    public DnsRecord(final String type, final String name, final String data) {
        this.data = data;
        this.name = name;
        this.protocol = null;
        this.service = null;
        this.type = type;
        this.port = null;
        this.priority = null;
        this.weight = null;
    }
}
