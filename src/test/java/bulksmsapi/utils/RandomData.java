package bulksmsapi.utils;

import java.util.UUID;

/** Keeps repeated test runs from colliding on unique columns (client_email, profile.msisdn, etc). */
public final class RandomData {

    private RandomData() {
    }

    public static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /** A syntactically valid Kenyan-looking MSISDN (12 digits, within profile.msisdn's 9-15 digit bound). */
    public static String uniqueMsisdn() {
        long suffix = Math.abs(UUID.randomUUID().getLeastSignificantBits()) % 100_000_000L;
        return String.format("2547%08d", suffix);
    }
}
