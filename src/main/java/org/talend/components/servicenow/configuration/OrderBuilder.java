package org.talend.components.servicenow.configuration;

import static org.talend.components.servicenow.configuration.CommonConfig.Proposable_GetTableFields;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Proposable;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@GridLayout({
        @GridLayout.Row({ "field" }),
        @GridLayout.Row({ "order" }),
})
public class OrderBuilder {

    @Option
    @Proposable(Proposable_GetTableFields)
    @Documentation("filed to be ordered")
    private String field;

    @Option
    @Documentation("the order")
    private Order order;

    public OrderBuilder(final QueryBuilder.Fields field, final Order order) {
        this.field = field.name();
        this.order = order;
    }

    public enum Order {
        DESC,
        ASC;
    }
}
