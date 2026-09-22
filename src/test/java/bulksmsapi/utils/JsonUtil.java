package bulksmsapi.utils;

import java.util.List;
import java.util.Map;

/**
 * Hand-rolled JSON building for the request bodies this suite sends -
 * deliberately dependency-free (no Jackson/Gson needed). Values are
 * type-aware: String -> quoted, Number/Boolean -> raw literal, List -> JSON
 * array (each element run back through the same rules), null -> null. Covers
 * every payload shape this suite needs, from flat string bodies (SMS
 * requests, login) to ones with numeric fields (role_id, send_type_id) and
 * array fields (bulk campaign recipients).
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String toJsonObject(Map<String, ?> fields) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : fields.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(entry.getKey())).append("\":");
            sb.append(toJsonValue(entry.getValue()));
        }
        return sb.append("}").toString();
    }

    private static String toJsonValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJsonValue(list.get(i)));
            }
            return sb.append("]").toString();
        }
        return "\"" + escape(value.toString()) + "\"";
    }

    public static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
