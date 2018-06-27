package org.talend.components.servicenow.source;

import static java.util.stream.Collectors.toList;
import static org.talend.components.servicenow.configuration.TableDataSet.READ_ALL_RECORD_FROM_SERVER;
import static org.talend.components.servicenow.service.http.TableApiClient.API_BASE;
import static org.talend.components.servicenow.service.http.TableApiClient.API_VERSION;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.LongStream;

import javax.annotation.PostConstruct;

import org.talend.components.servicenow.configuration.TableDataSet;
import org.talend.components.servicenow.messages.Messages;
import org.talend.components.servicenow.service.ServiceNowTableService;
import org.talend.components.servicenow.service.http.TableApiClient;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.Assessor;
import org.talend.sdk.component.api.input.Emitter;
import org.talend.sdk.component.api.input.PartitionMapper;
import org.talend.sdk.component.api.input.PartitionSize;
import org.talend.sdk.component.api.input.Split;
import org.talend.sdk.component.api.meta.Documentation;

@Version
@Icon(value = Icon.IconType.CUSTOM, custom = "ServiceNowInput")
@PartitionMapper(name = "ServiceNowInput")
@Documentation("ServiceNowInput is a configurable connector that is responsible of reading from Service Now Table using a query to filter the records.")
public class ServiceNowTableMapper implements Serializable {

    private final TableDataSet ds;

    private final ServiceNowTableService service;

    private final Messages i18n;

    private final TableApiClient tableAPI;

    public ServiceNowTableMapper(@Option("tableDataSet") final TableDataSet tableDataSet,
            final ServiceNowTableService service, final Messages i18n, TableApiClient tableAPI) {
        this.ds = tableDataSet;
        this.service = service;
        this.tableAPI = tableAPI;
        this.i18n = i18n;
    }

    @PostConstruct
    public void init() {
        tableAPI.base(ds.getDataStore().getUrlWithSlashEnding() + API_BASE + "/" + API_VERSION);
    }

    @Assessor
    public long estimateSize() {
        final int totalData = tableAPI.count(ds.getCommonConfig().getTableName().name(),
                ds.getDataStore().getAuthorizationHeader(),
                ds.buildQuery());

        final int requestedSize =
                ds.getMaxRecords() == READ_ALL_RECORD_FROM_SERVER ? totalData : Math.min(totalData, ds.getMaxRecords());

        long recordSize = tableAPI.estimateRecordSize(ds.getCommonConfig().getTableName().name(),
                ds.getDataStore().getAuthorizationHeader(), ds.buildQuery(),
                ds.getCommonConfig().getFieldsCommaSeparated());

        return recordSize * requestedSize;
    }

    @Split
    public List<ServiceNowTableMapper> split(@PartitionSize final long bundles) {

        long recordSize =
                tableAPI.estimateRecordSize(ds.getCommonConfig().getTableName().name(),
                        ds.getDataStore().getAuthorizationHeader(), ds.buildQuery(),
                        ds.getCommonConfig().getFieldsCommaSeparated());

        long nbBundle = Math.max(1, estimateSize() / bundles);
        final long bundleCount = bundles / recordSize;
        final int totalData = tableAPI.count(ds.getCommonConfig().getTableName().name(),
                ds.getDataStore().getAuthorizationHeader(),
                ds.buildQuery());

        final int requestedSize = ds.getMaxRecords() == READ_ALL_RECORD_FROM_SERVER ?
                totalData : Math.min(totalData, ds.getMaxRecords());

        return LongStream.range(0, nbBundle).mapToObj(i -> {
            final int from = (int) (bundleCount * i);
            final int to = (i == nbBundle - 1) ? requestedSize : (int) (from + bundleCount);
            if (to == 0) {
                return null;
            }
            final TableDataSet dataSetChunk = new TableDataSet(ds);
            dataSetChunk.setOffset(from);
            dataSetChunk.setMaxRecords(to);
            return new ServiceNowTableMapper(dataSetChunk, service, i18n, tableAPI);
        }).filter(Objects::nonNull).collect(toList());
    }

    @Emitter
    public ServiceNowTableSource createWorker() {
        return new ServiceNowTableSource(ds, i18n, tableAPI);
    }
}