package org.talend.components.servicenow.output;

import static org.talend.components.servicenow.service.http.TableApiClient.API_BASE;
import static org.talend.components.servicenow.service.http.TableApiClient.API_VERSION;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.talend.components.servicenow.configuration.OutputConfig;
import org.talend.components.servicenow.service.http.TableApiClient;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Output;
import org.talend.sdk.component.api.processor.OutputEmitter;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.service.http.HttpException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Version
@Icon(value = Icon.IconType.CUSTOM, custom = "ServiceNowOutput")
@Processor(name = "ServiceNowOutput")
@Documentation("ServiceNowOutput is a configurable connector able to write records to Service Now Table")
public class ServiceNowOutput implements Serializable {

    private final OutputConfig outputConfig;

    private final JsonBuilderFactory factory;

    TableApiClient client;

    public ServiceNowOutput(@Option("configuration") final OutputConfig outputConfig, TableApiClient client,
            JsonBuilderFactory factory) {
        this.outputConfig = outputConfig;
        this.client = client;
        this.factory = factory;
    }

    @PostConstruct
    public void init() {
        client.base(outputConfig.getDataStore().getUrlWithSlashEnding() + API_BASE + "/" + API_VERSION);
    }

    @ElementListener
    public void onNext(@Input final JsonObject record,
            final @Output OutputEmitter<JsonObject> success,
            final @Output("reject") OutputEmitter<Reject> reject) {
        try {
            JsonObject newRec;
            String sysId = null;
            if (record.containsKey("sys_id")) {
                sysId = record.getString("sys_id");
            }
            switch (outputConfig.getActionOnTable()) {
            case Insert:
                final JsonObject copy = sysId != null && sysId.isEmpty() ?
                        record.entrySet().stream()
                                .filter(e -> !e.getKey().equals("sys_id"))
                                .collect(factory::createObjectBuilder,
                                        (b, a) -> b.add(a.getKey(), a.getValue()),
                                        JsonObjectBuilder::addAll).build() : record;
                newRec = client.create(outputConfig.getCommonConfig().getTableName().name(),
                        outputConfig.getDataStore().getAuthorizationHeader(),
                        outputConfig.isNoResponseBody(),
                        copy);
                if (newRec != null) {
                    success.emit(newRec);
                }
                break;
            case Update:
                if (sysId == null || sysId.isEmpty()) {
                    reject.emit(new Reject(1, "sys_id is required to update the record", null, record));
                } else {
                    newRec = client.update(outputConfig.getCommonConfig().getTableName().name(), sysId,
                            outputConfig.getDataStore().getAuthorizationHeader(), outputConfig.isNoResponseBody(),
                            record);

                    if (newRec != null) {
                        success.emit(newRec);
                    }
                }
                break;
            case Delete:
                if (sysId == null || sysId.isEmpty()) {
                    reject.emit(new Reject(2, "sys_id is required to delete the record", null, record));
                } else {
                    client.deleteRecordById(outputConfig.getCommonConfig().getTableName().name(), sysId,
                            outputConfig.getDataStore().getAuthorizationHeader());
                    success.emit(record);
                }
                break;
            default:
                throw new UnsupportedOperationException(outputConfig.getActionOnTable() + " is not supported yet");
            }

        } catch (HttpException httpError) {
            final JsonObject error = (JsonObject) httpError.getResponse().error(JsonObject.class);
            if (error != null && error.containsKey("error")) {
                reject.emit(new Reject(httpError.getResponse().status(),
                        error.getJsonObject("error").getString("message"),
                        error.getJsonObject("error").getString("detail"),
                        record));
            } else {
                reject.emit(new Reject(httpError.getResponse().status(), "unknown", "unknown", record));
            }
        }
    }
}