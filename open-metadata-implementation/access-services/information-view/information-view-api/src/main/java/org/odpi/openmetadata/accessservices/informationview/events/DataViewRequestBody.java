/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.accessservices.informationview.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;


@JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataViewRequestBody extends InformationViewHeader{

    private String registrationGuid;
    private String registrationQualifiedName;
    @Valid
    @NotNull
    private DataView dataView;

    public DataView getDataView() {
        return dataView;
    }

    public void setDataView(DataView dataView) {
        this.dataView = dataView;
    }

    /**
     *
     * @return guid associated to the external tool at registration step
     */
    public String getRegistrationGuid() {
        return registrationGuid;
    }

    /**
     *
     * @param registrationGuid - guid of the external tool
     */
    public void setRegistrationGuid(String registrationGuid) {
        this.registrationGuid = registrationGuid;
    }

    public String getRegistrationQualifiedName() {
        return registrationQualifiedName;
    }

    public void setRegistrationQualifiedName(String registrationQualifiedName) {
        this.registrationQualifiedName = registrationQualifiedName;
    }

    @Override
    public String toString() {
        return "{" +
                "registrationGuid='" + registrationGuid + '\'' +
                ", registrationQualifiedName='" + registrationQualifiedName + '\'' +
                ", dataView=" + dataView +
                '}';
    }
}
