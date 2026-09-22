package bulksmsapi.stepdefinitions;

import io.restassured.response.Response;

public class ResponseContext {
    private static final ThreadLocal<Response> responseHolder = new ThreadLocal<>();

    public static void set(Response response) {
        responseHolder.set(response);
    }

    public static Response get() {
        Response r = responseHolder.get();
        if (r == null) throw new IllegalStateException("No response set in this scenario yet");
        return r;
    }

    public static void clear() {
        responseHolder.remove();
    }
}
