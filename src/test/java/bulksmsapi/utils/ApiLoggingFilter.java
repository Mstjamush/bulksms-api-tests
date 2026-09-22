package bulksmsapi.utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs every request/response that goes through RestAssured - registered
 * once globally (see HttpLogging.install()) so no step definition or the
 * TestRail client has to opt in individually. One INFO line per call
 * (method, URI, status, duration); bodies only at DEBUG, since request/
 * response bodies can contain the signed headers' raw payloads.
 */
public class ApiLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger("bulksmsapi.http");

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                            FilterableResponseSpecification responseSpec,
                            FilterContext ctx) {
        String method = requestSpec.getMethod();
        String uri = requestSpec.getURI();

        if (log.isDebugEnabled() && requestSpec.getBody() != null) {
            log.debug(">> {} {} body: {}", method, uri, requestSpec.getBody());
        }

        long startedAt = System.currentTimeMillis();
        Response response = ctx.next(requestSpec, responseSpec);
        long tookMs = System.currentTimeMillis() - startedAt;

        log.info("{} {} -> {} ({} ms)", method, uri, response.getStatusCode(), tookMs);
        if (log.isDebugEnabled()) {
            log.debug("<< {} body: {}", response.getStatusCode(), response.getBody().asString());
        }

        return response;
    }
}
