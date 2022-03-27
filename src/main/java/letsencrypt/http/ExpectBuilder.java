package letsencrypt.http;

import java.io.IOException;
import java.util.function.Supplier;

public interface ExpectBuilder extends RequestExecutor {
    ExpectBuilder expectStatusCode(int code);
    ExpectBuilder expectSuccess();
    ExpectBuilder expectSuccess(Supplier<? extends IOException> supplier);
    ExpectBuilder expect(Expect<String> expect);
}
