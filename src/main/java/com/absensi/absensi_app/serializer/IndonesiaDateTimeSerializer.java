package com.absensi.absensi_app.serializer;

import com.absensi.absensi_app.util.DateTimeFormatterUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;

public class IndonesiaDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        if (value != null) {
            gen.writeString(value.format(DateTimeFormatterUtil.DATE_TIME_FORMAT));
        }
    }
}
