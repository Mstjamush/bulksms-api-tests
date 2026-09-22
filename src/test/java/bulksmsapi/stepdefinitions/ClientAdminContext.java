package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.AdminFixtures.ProvisionedClient;
import bulksmsapi.utils.AdminFixtures.ProvisionedUser;

/**
 * Scenario-scoped memory of "the client currently in play" (and the last
 * sub-user/sender created off it) - lets steps in different classes
 * (clients/users/senders/bulk-campaigns) chain off state set up earlier in
 * the same scenario, the same way ResponseContext carries the last response.
 */
public class ClientAdminContext {

    private static final ThreadLocal<ProvisionedClient> clientHolder = new ThreadLocal<>();
    private static final ThreadLocal<ProvisionedUser> lastCreatedUserHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> lastSenderShortCodeHolder = new ThreadLocal<>();

    public static void setClient(ProvisionedClient client) {
        clientHolder.set(client);
    }

    public static void setLastCreatedUser(ProvisionedUser user) {
        lastCreatedUserHolder.set(user);
    }

    public static void setLastSenderShortCode(String shortCode) {
        lastSenderShortCodeHolder.set(shortCode);
    }

    public static ProvisionedClient client() {
        ProvisionedClient c = clientHolder.get();
        if (c == null) throw new IllegalStateException("No client set in this scenario yet");
        return c;
    }

    public static ProvisionedUser lastCreatedUser() {
        ProvisionedUser u = lastCreatedUserHolder.get();
        if (u == null) throw new IllegalStateException("No user created in this scenario yet");
        return u;
    }

    public static String lastSenderShortCode() {
        String s = lastSenderShortCodeHolder.get();
        if (s == null) throw new IllegalStateException("No sender created in this scenario yet");
        return s;
    }

    public static void clear() {
        clientHolder.remove();
        lastCreatedUserHolder.remove();
        lastSenderShortCodeHolder.remove();
    }
}
