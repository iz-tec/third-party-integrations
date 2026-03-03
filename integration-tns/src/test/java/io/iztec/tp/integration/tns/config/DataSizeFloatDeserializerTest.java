package io.iztec.tp.integration.tns.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DataSizeFloatDeserializer.
 * No Spring context — pure logic test.
 */
class DataSizeFloatDeserializerTest {

    private DataSizeFloatDeserializer deserializer;
    private JsonParser parser;
    private DeserializationContext ctx;

    @BeforeEach
    void setUp() {
        deserializer = new DataSizeFloatDeserializer();
        parser = mock(JsonParser.class);
        ctx = mock(DeserializationContext.class);
    }

    @ParameterizedTest(name = "\"{0}\" should convert to {1} bytes")
    @CsvSource({
            "0.00 B,   0.0",
            "1.00 B,   1.0",
            "1.50 KB,  1536.0",
            "1.00 KB,  1024.0",
            "20.00 MB, 20971520.0",
            "1.00 MB,  1048576.0"
    })
    void deserialize_validInputs_shouldConvertToBytes(String raw, float expected) throws IOException {
        when(parser.getText()).thenReturn(raw);

        Float result = deserializer.deserialize(parser, ctx);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void deserialize_nullValue_shouldReturnNull() throws IOException {
        when(parser.getText()).thenReturn(null);

        Float result = deserializer.deserialize(parser, ctx);

        assertThat(result).isNull();
    }

    @Test
    void deserialize_blankValue_shouldReturnNull() throws IOException {
        when(parser.getText()).thenReturn("   ");

        Float result = deserializer.deserialize(parser, ctx);

        assertThat(result).isNull();
    }

    @Test
    void deserialize_malformedFormat_missingUnit_shouldThrow() throws IOException {
        when(parser.getText()).thenReturn("20.00");

        assertThatThrownBy(() -> deserializer.deserialize(parser, ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unexpected data-size format");
    }

    @Test
    void deserialize_unknownUnit_shouldThrow() throws IOException {
        when(parser.getText()).thenReturn("20.00 GB");

        assertThatThrownBy(() -> deserializer.deserialize(parser, ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown data-size unit");
    }
}

