package org.talend.components.servicenow.configuration;

import static org.talend.components.servicenow.configuration.CommonConfig.Proposable_GetTableFields;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Proposable;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.service.completion.DynamicValues;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@GridLayout({
        @GridLayout.Row({ "field" }),
        @GridLayout.Row({ "operation" }),
        @GridLayout.Row({ "value" })
})
public class QueryBuilder implements Serializable {

    @Option
    @Proposable(Proposable_GetTableFields)
    @Documentation("field name")
    private String field;

    @Option
    @Documentation("operation")
    private Operation operation;

    @Option
    @Documentation("value")
    private String value = "";

    public enum Operation {
        Equals("="),
        Not_Equals("!="),
        Greater_Than(">"),
        Greater_Than_OR_Equals(">="),
        Less_Than("<"),
        Less_Than_Or_Equals("<=");

        private String op;

        Operation(final String op) {
            this.op = op;
        }

        public String operation() {
            return op;
        }
    }

    public QueryBuilder(final Fields field, final Operation operation, final String value) {
        this.field = field.name();
        this.operation = operation;
        this.value = value;
    }

    public enum Fields {
        parent,
        category,
        made_sla,
        watch_list,
        upon_reject,
        sys_updated_on,
        approval_history,
        number,
        user_input,
        sys_created_on,
        delivery_plan,
        impact,
        active,
        work_notes_list,
        business_service,
        priority,
        sys_domain_path,
        time_worked,
        expected_start,
        rejection_goto,
        opened_at,
        business_duration,
        group_list,
        work_end,
        approval_set,
        wf_activity,
        work_notes,
        short_description,
        correlation_display,
        delivery_task,
        work_start,
        assignment_group,
        additional_assignee_list,
        description,
        calendar_duration,
        close_notes,
        sys_class_name,
        closed_by,
        follow_up,
        contact_type,
        urgency,
        company,
        reassignment_count,
        activity_due,
        assigned_to,
        comments,
        approval,
        sla_due,
        comments_and_work_notes,
        due_date,
        sys_mod_count,
        sys_tags,
        escalation,
        upon_approval,
        correlation_id,
        location;
    }

}
