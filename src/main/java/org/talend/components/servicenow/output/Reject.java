package org.talend.components.servicenow.output;

import java.io.Serializable;

import javax.json.JsonObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reject implements Serializable {

    private int code;

    private String errorMessage;

    private String errorDetail;

    private JsonObject record;

}
