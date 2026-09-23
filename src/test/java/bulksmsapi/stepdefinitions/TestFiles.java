package bulksmsapi.stepdefinitions;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

final class TestFiles {

    private TestFiles() {
    }

    static byte[] read(String name) {
        String path = "testdata/" + name;
        try (InputStream is = TestFiles.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("Missing test resource: " + path);
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String mimeType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".csv")) return "text/csv";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        return "application/octet-stream";
    }
}
