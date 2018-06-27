package org.talend.components.servicenow.configuration;

import static org.talend.components.servicenow.configuration.BasicAuthConfig.NAME;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Checkable;
import org.talend.sdk.component.api.configuration.action.Validable;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Credential;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Checkable(NAME)
@DataStore(NAME)
@GridLayout({
        @GridLayout.Row({ "url" }),
        @GridLayout.Row({ "username", "password" }),
})
@Documentation("Basic auth data store for Service Now")
public class BasicAuthConfig implements Serializable {

    public static final String NAME = "basicAuth";

    @Option
    @Validable("urlValidation") // in practise a @Pattern is better probably, for demo purposes
    @Documentation("Service Now API instance URL")
    private String url;

    @Option
    @Documentation("Service Now Instance username")
    private String username;

    @Option
    @Credential
    @Documentation("Service Now Instance password")
    private String password;

    public String getUrlWithSlashEnding() {
        if (this.url == null) {
            return null;
        }

        String urlWithSlash = this.url;
        if (!url.endsWith("/")) {
            urlWithSlash += "/";
        }
        return urlWithSlash;
    }

    public String getAuthorizationHeader() {
        try {
            return "Basic " + Base64.getEncoder()
                    .encodeToString((this.getUsername() + ":" + this.getPassword()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
