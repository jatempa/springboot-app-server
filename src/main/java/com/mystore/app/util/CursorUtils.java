package com.mystore.app.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class CursorUtils {

    private CursorUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String encodeCursor(Instant timestamp, Integer id) {
        String raw = timestamp.getEpochSecond() + ":" + timestamp.getNano() + ":" + id;
        return Base64.getUrlEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static CursorData decodeCursor(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split(":");
            if (parts.length != 3) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cursor format");
            }
            long epochSecond = Long.parseLong(parts[0]);
            int nano = Integer.parseInt(parts[1]);
            int id = Integer.parseInt(parts[2]);
            return new CursorData(Instant.ofEpochSecond(epochSecond, nano), id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cursor", e);
        }
    }

    public record CursorData(Instant timestamp, Integer id) {
    }
}
