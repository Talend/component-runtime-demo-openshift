package org.talend.components.servicenow.service.http.codec;

import java.lang.reflect.Type;

import org.talend.components.servicenow.messages.Messages;
import org.talend.sdk.component.api.service.http.ContentType;
import org.talend.sdk.component.api.service.http.Decoder;

import lombok.AllArgsConstructor;

/**
 * We don't expect the api to send any non application/json content,
 * this only for error handling when the instance is not up & running
 */

@ContentType
@AllArgsConstructor
public class InvalidContentDecoder implements Decoder {

    private final Messages i18n;

    @Override
    public Object decode(final byte[] value, final Type expectedType) {
        if (value == null || value.length == 0) {
            return null;
        }

        throw new RuntimeException(i18n.invalidContent());
    }
}
