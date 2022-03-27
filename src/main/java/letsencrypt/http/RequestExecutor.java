package letsencrypt.http;

import java.io.IOException;
import java.lang.reflect.Type;

public interface RequestExecutor {
    <T> T fetchInto(Type type) throws IOException;
    <T> T fetchInto(Class<T> clazz) throws IOException;
    void execute() throws IOException;
}
