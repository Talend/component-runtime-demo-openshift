/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.talend.components.servicenow.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.talend.components.servicenow.configuration.BasicAuthConfig.NAME;
import static org.talend.components.servicenow.service.http.TableApiClient.API_BASE;
import static org.talend.components.servicenow.service.http.TableApiClient.API_VERSION;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.talend.components.servicenow.configuration.BasicAuthConfig;
import org.talend.components.servicenow.configuration.CommonConfig;
import org.talend.components.servicenow.configuration.QueryBuilder;
import org.talend.components.servicenow.configuration.TableDataSet;
import org.talend.components.servicenow.messages.Messages;
import org.talend.components.servicenow.service.http.TableApiClient;
import org.talend.components.servicenow.source.ServiceNowTableSource;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.asyncvalidation.AsyncValidation;
import org.talend.sdk.component.api.service.asyncvalidation.ValidationResult;
import org.talend.sdk.component.api.service.cache.Cached;
import org.talend.sdk.component.api.service.completion.DynamicValues;
import org.talend.sdk.component.api.service.completion.Values;
import org.talend.sdk.component.api.service.healthcheck.HealthCheck;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
import org.talend.sdk.component.api.service.http.HttpClient;
import org.talend.sdk.component.api.service.http.HttpException;
import org.talend.sdk.component.api.service.http.Request;
import org.talend.sdk.component.api.service.http.Response;
import org.talend.sdk.component.api.service.schema.DiscoverSchema;
import org.talend.sdk.component.api.service.schema.Schema;
import org.talend.sdk.component.api.service.schema.Type;

@Service
public class ServiceNowTableService {

    @HealthCheck(value = NAME)
    public HealthCheckStatus healthCheck(@Option(BasicAuthConfig.NAME) BasicAuthConfig dt, TableApiClient client,
            Messages i18n) {
        client.base(dt.getUrlWithSlashEnding() + API_BASE + "/" + API_VERSION);
        try {
            client.healthCheck(dt.getAuthorizationHeader());
        } catch (Exception e) {
            if (HttpException.class.isInstance(e)) {
                final HttpException ex = HttpException.class.cast(e);
                final JsonObject jError = JsonObject.class.cast(ex.getResponse().error(JsonObject.class));
                String errorMessage = null;
                if (jError != null && jError.containsKey("error")) {
                    final JsonObject error = jError.get("error").asJsonObject();
                    errorMessage = error.getString("message") + " \n" + error.getString("detail");
                }
                return new HealthCheckStatus(HealthCheckStatus.Status.KO,
                        i18n.connectionFailed(errorMessage != null && errorMessage.trim().isEmpty() ?
                                e.getLocalizedMessage() :
                                errorMessage));
            }

            return new HealthCheckStatus(HealthCheckStatus.Status.KO, i18n.connectionFailed(e.getLocalizedMessage()));
        }

        return new HealthCheckStatus(HealthCheckStatus.Status.OK, i18n.connectionSuccessful());
    }

    @AsyncValidation("urlValidation")
    public ValidationResult validateUrl(final String url) {
        try {
            new URL(url);
            return new ValidationResult(ValidationResult.Status.OK, null);
        } catch (MalformedURLException e) {
            return new ValidationResult(ValidationResult.Status.KO, e.getMessage());
        }
    }

    @Cached
    @DynamicValues(CommonConfig.Proposable_GetTableFields)
    public Values getTableFields(final Client client) {
        // todo when dynamic values can have params
        return new Values(Stream.of(QueryBuilder.Fields.values())
                .map(f -> new Values.Item(f.name(), f.name()))
                .collect(toList()));
    }

    public interface Client extends HttpClient {
        @Request
        Response<String> whataver();
    }

    @DiscoverSchema("guessTableSchema")
    public Schema guessTableSchema(final TableDataSet dataSet, final TableApiClient client, final Messages i18n) {
        dataSet.setMaxRecords(1); // limit result to 1 record to infer the schema
        if (dataSet.getCommonConfig() != null) {
            //we want to retrieve all the fields for the guess schema
            dataSet.getCommonConfig().setFields(null);
        }
        final ServiceNowTableSource source = new ServiceNowTableSource(dataSet, i18n, client);
        source.init();
        final JsonObject record = source.next();
        if (record == null || record.keySet().isEmpty()) {
            return new Schema(emptyList());
        }

        return new Schema(record.keySet()
                .stream()
                .map(k -> new Schema.Entry(k, Type.STRING))
                .collect(toList()));
    }

}
