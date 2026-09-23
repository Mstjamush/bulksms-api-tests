package bulksmsapi.stepdefinitions;

import java.util.HashMap;
import java.util.Map;

/** Scenario-scoped values (ids of lists, campaigns, plans... created earlier in
 * the same scenario) shared across step classes. Cleared before each scenario. */
public final class ScenarioData {

    private static final ThreadLocal<Map<String, Object>> DATA = ThreadLocal.withInitial(HashMap::new);

    private ScenarioData() {
    }

    public static void put(String key, Object value) {
        DATA.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        Object value = DATA.get().get(key);
        if (value == null) throw new IllegalStateException("Nothing stored as '" + key + "' in this scenario yet");
        return (T) value;
    }

    public static boolean has(String key) {
        return DATA.get().containsKey(key);
    }

    public static void clear() {
        DATA.get().clear();
    }
}
