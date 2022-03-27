package letsencrypt.util;

import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.exception.AcmeException;

public enum Exceptions {;

    public static AcmeException challengeFailedBeforeTried(final Authorization auth) {
        return new AcmeException("Challenge " + auth.getIdentifier() + " was already failed: " + auth.getStatus());
    }
    public static AcmeException challengeFailed(final Authorization auth) {
        return new AcmeException("We failed a challenge " + auth.getIdentifier());
    }
    public static AcmeException signingRequestFailed(final Order order) {
        return new AcmeException("We failed a signing request: " + order.getError());
    }

}
