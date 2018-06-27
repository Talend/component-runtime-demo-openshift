package org.talend.components.servicenow.service.http.codec;

import java.lang.reflect.Type;

import org.talend.sdk.component.api.service.http.ContentType;
import org.talend.sdk.component.api.service.http.Decoder;

@ContentType("application/json")
public class RecordSizeDecoder implements Decoder {

    @Override
    public Object decode(final byte[] value, final Type expectedType) {
        if (value == null) {
            return 0;
        }

        return Long.valueOf(value.length);
    }
}
