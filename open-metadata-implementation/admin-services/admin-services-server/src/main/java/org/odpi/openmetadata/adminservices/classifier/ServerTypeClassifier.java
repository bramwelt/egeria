/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adminservices.classifier;

import org.odpi.openmetadata.adminservices.configuration.properties.*;
import org.odpi.openmetadata.adminservices.configuration.registration.GovernanceServicesDescription;
import org.odpi.openmetadata.adminservices.configuration.registration.ServerTypeClassification;
import org.odpi.openmetadata.adminservices.ffdc.OMAGAdminErrorCode;
import org.odpi.openmetadata.adminservices.ffdc.exception.OMAGConfigurationErrorException;
import org.odpi.openmetadata.adminservices.ffdc.exception.OMAGInvalidParameterException;

import java.util.List;

/**
 * ServerTypeClassifier is responsible for reviewing the configuration document to determine what type of
 * server is being requested in the configuration document.
 */
public class ServerTypeClassifier
{
    private static final String ACCESS_SERVICES_NAME = "Open Metadata Access Services (OMAS)";

    private OMAGServerConfig  configurationDocument;
    private String            serverName;

    /**
     * Constructor
     *
     * @param serverName name of the server
     * @param configurationDocument document to analyze
     */
    public ServerTypeClassifier(String           serverName,
                                OMAGServerConfig configurationDocument)
    {
        this.configurationDocument = configurationDocument;
        this.serverName = serverName;
    }


    /**
     * Check that the config document will result in a valid type of server and return the type.
     *
     * @return server type classification or exception if anything wrong
     * @throws OMAGInvalidParameterException null config doc
     * @throws OMAGConfigurationErrorException incompatible config
     */
    public ServerTypeClassification getServerType() throws OMAGInvalidParameterException,
                                                           OMAGConfigurationErrorException
    {
        final String methodName = "getServerType";

        ServerTypeClassification serverTypeClassification = null;

        this.validateConfigurationDocumentNotNull(serverName, configurationDocument, methodName);

        RepositoryServicesConfig        repositoryServicesConfig        = configurationDocument.getRepositoryServicesConfig();
        List<AccessServiceConfig>       accessServiceConfigList         = configurationDocument.getAccessServicesConfig();
        ConformanceSuiteConfig          conformanceSuiteConfig          = configurationDocument.getConformanceSuiteConfig();
        DiscoveryEngineServicesConfig   discoveryEngineServicesConfig   = configurationDocument.getDiscoveryEngineServicesConfig();
        OpenLineageServerConfig         openLineageServerConfig         = configurationDocument.getOpenLineageServerConfig();
        SecuritySyncConfig              securitySyncConfig              = configurationDocument.getSecuritySyncConfig();
        SecurityOfficerConfig           securityOfficerConfig           = configurationDocument.getSecurityOfficerConfig();
        StewardshipEngineServicesConfig stewardshipEngineServicesConfig = configurationDocument.getStewardshipEngineServicesConfig();
        VirtualizationConfig            virtualizationConfig            = configurationDocument.getVirtualizationConfig();
        DataEngineProxyConfig           dataEngineProxyConfig           = configurationDocument.getDataEngineProxyConfig();
        DataPlatformServicesConfig      dataPlatformServicesConfig      = configurationDocument.getDataPlatformServicesConfig();

        if ((repositoryServicesConfig == null) &&
                (accessServiceConfigList == null) &&
                (conformanceSuiteConfig == null) &&
                (discoveryEngineServicesConfig == null) &&
                (openLineageServerConfig == null) &&
                (securitySyncConfig == null) &&
                (securityOfficerConfig == null) &&
                (stewardshipEngineServicesConfig == null) &&
                (virtualizationConfig == null) &&
                (dataEngineProxyConfig == null) &&
                (dataPlatformServicesConfig == null))
        {
            OMAGAdminErrorCode errorCode    = OMAGAdminErrorCode.EMPTY_CONFIGURATION;
            String             errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(serverName);

            throw new OMAGConfigurationErrorException(errorCode.getHTTPErrorCode(),
                                                      this.getClass().getName(),
                                                      methodName,
                                                      errorMessage,
                                                      errorCode.getSystemAction(),
                                                      errorCode.getUserAction());
        }


        /*
         * If there is a mismatch in the server name inside the configuration document and the
         * requested server name it means there is an error in either the implementation or
         * configuration of the configuration document store.
         */
        this.validateConfigServerName(serverName, configurationDocument.getLocalServerName(), methodName);

        /*
         * All servers need the repository services
         */
        if (repositoryServicesConfig == null)
        {
            /*
             * To get here, then another service is configured but not the repository services.
             */
            OMAGAdminErrorCode errorCode    = OMAGAdminErrorCode.NULL_REPOSITORY_CONFIG;
            String             errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(serverName);

            throw new OMAGConfigurationErrorException(errorCode.getHTTPErrorCode(),
                                                      this.getClass().getName(),
                                                      methodName,
                                                      errorMessage,
                                                      errorCode.getSystemAction(),
                                                      errorCode.getUserAction());
        }

        /*
         * The access service list is only allowed in a metadata server or metadata access point.
         */
        if (accessServiceConfigList != null)
        {
            if (this.detectLocalRepository(repositoryServicesConfig))
            {
                serverTypeClassification = ServerTypeClassification.METADATA_SERVER;
            }
            else
            {
                serverTypeClassification = ServerTypeClassification.METADATA_ACCESS_POINT;
            }

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.CONFORMANCE_SUITE_SERVICES.getServiceName(),
                                                conformanceSuiteConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_ENGINE_PROXY_SERVICES.getServiceName(),
                                                dataEngineProxyConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_PLATFORM_SERVICES.getServiceName(),
                                                dataPlatformServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DISCOVERY_ENGINE_SERVICES.getServiceName(),
                                                discoveryEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.OPEN_LINEAGE_SERVICES.getServiceName(),
                                                openLineageServerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_SYNC_SERVICES.getServiceName(),
                                                securitySyncConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_OFFICER_SERVICES.getServiceName(),
                                                securityOfficerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.STEWARDSHIP_SERVICES.getServiceName(),
                                                stewardshipEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.VIRTUALIZATION_SERVICES.getServiceName(),
                                                virtualizationConfig);
        }

        if (conformanceSuiteConfig != null)
        {
            serverTypeClassification = ServerTypeClassification.CONFORMANCE_SERVER;

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                ACCESS_SERVICES_NAME,
                                                accessServiceConfigList);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_ENGINE_PROXY_SERVICES.getServiceName(),
                                                dataEngineProxyConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_PLATFORM_SERVICES.getServiceName(),
                                                dataPlatformServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DISCOVERY_ENGINE_SERVICES.getServiceName(),
                                                discoveryEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.OPEN_LINEAGE_SERVICES.getServiceName(),
                                                openLineageServerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_SYNC_SERVICES.getServiceName(),
                                                securitySyncConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_OFFICER_SERVICES.getServiceName(),
                                                securityOfficerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.STEWARDSHIP_SERVICES.getServiceName(),
                                                stewardshipEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.VIRTUALIZATION_SERVICES.getServiceName(),
                                                virtualizationConfig);
        }

        if (dataEngineProxyConfig != null)
        {
            serverTypeClassification = ServerTypeClassification.DATA_ENGINE_PROXY;

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                ACCESS_SERVICES_NAME,
                                                accessServiceConfigList);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.CONFORMANCE_SUITE_SERVICES.getServiceName(),
                                                conformanceSuiteConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_PLATFORM_SERVICES.getServiceName(),
                                                dataPlatformServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DISCOVERY_ENGINE_SERVICES.getServiceName(),
                                                discoveryEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.OPEN_LINEAGE_SERVICES.getServiceName(),
                                                openLineageServerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_SYNC_SERVICES.getServiceName(),
                                                securitySyncConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_OFFICER_SERVICES.getServiceName(),
                                                securityOfficerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.STEWARDSHIP_SERVICES.getServiceName(),
                                                stewardshipEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.VIRTUALIZATION_SERVICES.getServiceName(),
                                                virtualizationConfig);
        }

        if (dataPlatformServicesConfig != null)
        {
            serverTypeClassification = ServerTypeClassification.DATA_PLATFORM_SERVER;

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                ACCESS_SERVICES_NAME,
                                                accessServiceConfigList);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.CONFORMANCE_SUITE_SERVICES.getServiceName(),
                                                conformanceSuiteConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_ENGINE_PROXY_SERVICES.getServiceName(),
                                                dataEngineProxyConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DISCOVERY_ENGINE_SERVICES.getServiceName(),
                                                discoveryEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.OPEN_LINEAGE_SERVICES.getServiceName(),
                                                openLineageServerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_SYNC_SERVICES.getServiceName(),
                                                securitySyncConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_OFFICER_SERVICES.getServiceName(),
                                                securityOfficerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.STEWARDSHIP_SERVICES.getServiceName(),
                                                stewardshipEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.VIRTUALIZATION_SERVICES.getServiceName(),
                                                virtualizationConfig);
        }

        if (discoveryEngineServicesConfig != null)
        {
            serverTypeClassification = ServerTypeClassification.DISCOVERY_SERVER;

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                ACCESS_SERVICES_NAME,
                                                accessServiceConfigList);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.CONFORMANCE_SUITE_SERVICES.getServiceName(),
                                                conformanceSuiteConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_ENGINE_PROXY_SERVICES.getServiceName(),
                                                dataEngineProxyConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_PLATFORM_SERVICES.getServiceName(),
                                                dataPlatformServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.OPEN_LINEAGE_SERVICES.getServiceName(),
                                                openLineageServerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_SYNC_SERVICES.getServiceName(),
                                                securitySyncConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_OFFICER_SERVICES.getServiceName(),
                                                securityOfficerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.STEWARDSHIP_SERVICES.getServiceName(),
                                                stewardshipEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.VIRTUALIZATION_SERVICES.getServiceName(),
                                                virtualizationConfig);
        }

        if (openLineageServerConfig != null)
        {
            serverTypeClassification = ServerTypeClassification.OPEN_LINEAGE_SERVER;

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                ACCESS_SERVICES_NAME,
                                                accessServiceConfigList);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.CONFORMANCE_SUITE_SERVICES.getServiceName(),
                                                conformanceSuiteConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_ENGINE_PROXY_SERVICES.getServiceName(),
                                                dataEngineProxyConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_PLATFORM_SERVICES.getServiceName(),
                                                dataPlatformServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DISCOVERY_ENGINE_SERVICES.getServiceName(),
                                                discoveryEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_SYNC_SERVICES.getServiceName(),
                                                securitySyncConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_OFFICER_SERVICES.getServiceName(),
                                                securityOfficerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.STEWARDSHIP_SERVICES.getServiceName(),
                                                stewardshipEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.VIRTUALIZATION_SERVICES.getServiceName(),
                                                virtualizationConfig);
        }

        if (securitySyncConfig != null)
        {
            serverTypeClassification = ServerTypeClassification.SECURITY_SYNC_SERVER;

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                ACCESS_SERVICES_NAME,
                                                accessServiceConfigList);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.CONFORMANCE_SUITE_SERVICES.getServiceName(),
                                                conformanceSuiteConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_ENGINE_PROXY_SERVICES.getServiceName(),
                                                dataEngineProxyConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_PLATFORM_SERVICES.getServiceName(),
                                                dataPlatformServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DISCOVERY_ENGINE_SERVICES.getServiceName(),
                                                discoveryEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.OPEN_LINEAGE_SERVICES.getServiceName(),
                                                openLineageServerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_OFFICER_SERVICES.getServiceName(),
                                                securityOfficerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.STEWARDSHIP_SERVICES.getServiceName(),
                                                stewardshipEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.VIRTUALIZATION_SERVICES.getServiceName(),
                                                virtualizationConfig);
        }

        if (securityOfficerConfig != null)
        {
            serverTypeClassification = ServerTypeClassification.SECURITY_OFFICER_SERVER;

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                ACCESS_SERVICES_NAME,
                                                accessServiceConfigList);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.CONFORMANCE_SUITE_SERVICES.getServiceName(),
                                                conformanceSuiteConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_ENGINE_PROXY_SERVICES.getServiceName(),
                                                dataEngineProxyConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_PLATFORM_SERVICES.getServiceName(),
                                                dataPlatformServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DISCOVERY_ENGINE_SERVICES.getServiceName(),
                                                discoveryEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.OPEN_LINEAGE_SERVICES.getServiceName(),
                                                openLineageServerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_SYNC_SERVICES.getServiceName(),
                                                securitySyncConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.STEWARDSHIP_SERVICES.getServiceName(),
                                                stewardshipEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.VIRTUALIZATION_SERVICES.getServiceName(),
                                                virtualizationConfig);
        }

        if (stewardshipEngineServicesConfig != null)
        {
            serverTypeClassification = ServerTypeClassification.STEWARDSHIP_SERVER;

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                ACCESS_SERVICES_NAME,
                                                accessServiceConfigList);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.CONFORMANCE_SUITE_SERVICES.getServiceName(),
                                                conformanceSuiteConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_ENGINE_PROXY_SERVICES.getServiceName(),
                                                dataEngineProxyConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_PLATFORM_SERVICES.getServiceName(),
                                                dataPlatformServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DISCOVERY_ENGINE_SERVICES.getServiceName(),
                                                discoveryEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.OPEN_LINEAGE_SERVICES.getServiceName(),
                                                openLineageServerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_SYNC_SERVICES.getServiceName(),
                                                securitySyncConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_OFFICER_SERVICES.getServiceName(),
                                                securityOfficerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.VIRTUALIZATION_SERVICES.getServiceName(),
                                                virtualizationConfig);
        }

        if (virtualizationConfig != null)
        {
            serverTypeClassification = ServerTypeClassification.VIRTUALIZER_SERVER;

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                ACCESS_SERVICES_NAME,
                                                accessServiceConfigList);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.CONFORMANCE_SUITE_SERVICES.getServiceName(),
                                                conformanceSuiteConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_ENGINE_PROXY_SERVICES.getServiceName(),
                                                dataEngineProxyConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DATA_PLATFORM_SERVICES.getServiceName(),
                                                dataPlatformServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.DISCOVERY_ENGINE_SERVICES.getServiceName(),
                                                discoveryEngineServicesConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.OPEN_LINEAGE_SERVICES.getServiceName(),
                                                openLineageServerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_SYNC_SERVICES.getServiceName(),
                                                securitySyncConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.SECURITY_OFFICER_SERVICES.getServiceName(),
                                                securityOfficerConfig);

            this.validateSubsystemNotConfigured(serverName,
                                                serverTypeClassification.getServerTypeName(),
                                                GovernanceServicesDescription.STEWARDSHIP_SERVICES.getServiceName(),
                                                stewardshipEngineServicesConfig);
        }

        this.validateServerClassificationNotNull(serverName, serverTypeClassification, methodName);
        return serverTypeClassification;
    }


    /**
     * Checks that a configuration document has been supplied at server start up.
     *
     * @param serverName requested server
     * @param configurationDocument supplied document
     * @param methodName calling method
     * @throws OMAGInvalidParameterException resulting exception if config document is null.
     */
    private void validateConfigurationDocumentNotNull(String            serverName,
                                                      OMAGServerConfig  configurationDocument,
                                                      String            methodName) throws  OMAGInvalidParameterException
    {
        if (configurationDocument == null)
        {
            OMAGAdminErrorCode errorCode    = OMAGAdminErrorCode.NULL_SERVER_CONFIG;
            String             errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(serverName);

            throw new OMAGInvalidParameterException(errorCode.getHTTPErrorCode(),
                                                    this.getClass().getName(),
                                                    methodName,
                                                    errorMessage,
                                                    errorCode.getSystemAction(),
                                                    errorCode.getUserAction());
        }
    }


    /**
     * Checks that a classification has been derived for a configuration document.
     * This should never be called.  If it is occurring then there has probably been a new type
     * of server configuration added to Egeria without a corresponding update to the
     * server classifier.
     *
     * @param serverName requested server
     * @param serverTypeClassification classification value derived from the analysis of the
     *                                 configuration document
     * @param methodName calling method
     * @throws OMAGInvalidParameterException resulting exception if config document is null.
     */
    private void validateServerClassificationNotNull(String                    serverName,
                                                     ServerTypeClassification  serverTypeClassification,
                                                     String                    methodName) throws OMAGInvalidParameterException
    {
        if (serverTypeClassification == null)
        {
            OMAGAdminErrorCode errorCode    = OMAGAdminErrorCode.UNCLASSIFIABLE_SERVER;
            String             errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(serverName);

            throw new OMAGInvalidParameterException(errorCode.getHTTPErrorCode(),
                                                    this.getClass().getName(),
                                                    methodName,
                                                    errorMessage,
                                                    errorCode.getSystemAction(),
                                                    errorCode.getUserAction());
        }
    }


    /**
     * Determine if the server is to have a local repository or not.
     *
     * @param repositoryServicesConfig repository services config section of configuration document
     * @return true if there is a local repository, otherwise false.
     */
    private boolean detectLocalRepository(RepositoryServicesConfig  repositoryServicesConfig)
    {
        return repositoryServicesConfig.getLocalRepositoryConfig() != null;
    }


    /**
     * Check that the server configuration does not include a subsystem that it should not have.
     *
     * @param serverName name of the server that the configuration belongs to
     * @param serverTypeName name of the identified server type.
     * @param subsystemName name of subsystem being tested.
     * @param subsystemConfig setting of the configuration for the subsystem being tested.
     * @throws OMAGConfigurationErrorException inconsistent definition of subsystems for an OMAG server.
     */
    private void validateSubsystemNotConfigured(String serverName,
                                                String serverTypeName,
                                                String subsystemName,
                                                Object subsystemConfig) throws OMAGConfigurationErrorException
    {
        final String methodName = "validateSubsystemNotConfigured";

        if (subsystemConfig != null)
        {
            OMAGAdminErrorCode errorCode = OMAGAdminErrorCode.INCOMPATIBLE_SUBSYSTEMS;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(serverName,
                                                                                                     serverTypeName,
                                                                                                     subsystemName);

            throw new OMAGConfigurationErrorException(errorCode.getHTTPErrorCode(),
                                                      this.getClass().getName(),
                                                      methodName,
                                                      errorMessage,
                                                      errorCode.getSystemAction(),
                                                      errorCode.getUserAction());
        }
    }


    /**
     * Validate that the server name is not null and save it in the config.
     *
     * @param serverName  serverName passed on a request
     * @param configServerName serverName passed in config (should match request name)
     * @param methodName  method being called
     * @throws OMAGConfigurationErrorException incompatible server names
     */
    private void validateConfigServerName(String serverName,
                                          String configServerName,
                                          String methodName) throws OMAGConfigurationErrorException
    {
        if (! serverName.equals(configServerName))
        {
            OMAGAdminErrorCode errorCode = OMAGAdminErrorCode.INCOMPATIBLE_SERVER_NAMES;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(serverName,
                                                                                                            configServerName);

            throw new OMAGConfigurationErrorException(errorCode.getHTTPErrorCode(),
                                                      this.getClass().getName(),
                                                      methodName,
                                                      errorMessage,
                                                      errorCode.getSystemAction(),
                                                      errorCode.getUserAction());

        }
    }
}
