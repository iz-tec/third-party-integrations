package io.iztec.tp.integration.tns.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Converts TNS data-size strings into a Float value always expressed in bytes.
 *
 * TNS sends _f fields as human-readable strings: "0.00 B", "1.50 KB", "20.00 MB".
 * This deserializer parses the numeric part and converts the unit to bytes:
 *   B  → value × 1
 *   KB → value × 1_024
 *   MB → value × 1_048_576
 *
 * A null or blank JSON value is deserialized as null.
 */
public class DataSizeFloatDeserializer extends StdDeserializer<Float> {

    public DataSizeFloatDeserializer() {
        super(Float.class);
    }

    @Override
    public Float deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        String raw = p.getText();
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String[] parts = raw.trim().split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Unexpected data-size format: '" + raw + "'");
        }

        float value = Float.parseFloat(parts[0]);
        return switch (parts[1].toUpperCase()) {
            case "B"  -> value;
            case "KB" -> value * 1_024f;
            case "MB" -> value * 1_048_576f;
            default   -> throw new IllegalArgumentException("Unknown data-size unit: '" + parts[1] + "'");
        };
    }
}

