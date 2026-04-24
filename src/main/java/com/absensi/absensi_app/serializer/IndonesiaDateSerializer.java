package com.absensi.absensi_app.serializer;

import com.absensi.absensi_app.util.DateTimeFormatterUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;

// IndonesiaDateSerializer.java
public class IndonesiaDateSerializer extends JsonSerializer<LocalDate> {

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        if (value != null) {
            gen.writeString(value.format(DateTimeFormatterUtil.DATE_FORMAT)); // ✅
        }
    }
}

