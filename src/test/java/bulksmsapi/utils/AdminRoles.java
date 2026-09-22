package bulksmsapi.utils;

import java.util.Map;

/** Mirrors bulksms-api's app/admin/roles.py role ids - kept in sync by hand
 * since these are a fixed, seeded reference table (user_roles), not something
 * this suite reads dynamically. */
public final class AdminRoles {

    private AdminRoles() {
    }

    public static final int SUPER_ADMIN = 1;
    public static final int CLIENT_ADMIN = 2;
    public static final int DIGITAL_ADMIN = 3;
    public static final int SALES_AGENT = 4;
    public static final int NOVICE_ACCOUNT = 5;
    public static final int API_USER = 6;

    private static final Map<String, Integer> BY_NAME = Map.of(
            "Super Administrator", SUPER_ADMIN,
            "Client Administrator", CLIENT_ADMIN,
            "Digital Administrator", DIGITAL_ADMIN,
            "Sales Agent", SALES_AGENT,
            "Novice Account", NOVICE_ACCOUNT,
            "API User", API_USER
    );

    public static int idForName(String name) {
        Integer id = BY_NAME.get(name);
        if (id == null) throw new IllegalArgumentException("Unknown role name: " + name);
        return id;
    }
}
