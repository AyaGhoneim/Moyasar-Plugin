/*
 * Copyright 2021 Wovenware, Inc
 *
 * Wovenware licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may omain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.moyasar.core;

import java.util.Map;

import javax.annotation.Nullable;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.tenant.api.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoyasarHealthcheck implements Healthcheck {

    private static final Logger logger = LoggerFactory.getLogger(MoyasarHealthcheck.class);

    private final MoyasarConfigPropertiesConfigurationHandler moyasarConfigPropertiesConfigurationHandler;

    public MoyasarHealthcheck(final MoyasarConfigPropertiesConfigurationHandler moyasarConfigPropertiesConfigurationHandler) {
        this.moyasarConfigPropertiesConfigurationHandler = moyasarConfigPropertiesConfigurationHandler;
    }

    public HealthStatus getHealthStatus(@Nullable final Tenant tenant, @Nullable final Map properties) {
        if (tenant == null) {
            // The plugin is running
            return HealthStatus.healthy("Moyasar OK");
        } else {
            // Specifying the tenant lets you also validate the tenant configuration
            final MoyasarConfigProperties moyasarConfigProperties = moyasarConfigPropertiesConfigurationHandler.getConfigurable(tenant.getId());
            return pingMoyasar(moyasarConfigProperties);
        }
    }

    private HealthStatus pingMoyasar(final MoyasarConfigProperties moyasarConfigProperties) {
        final BraintreeGateway gateway = new BraintreeGateway(
                Environment.parseEnvironment(moyasarConfigProperties.getBtEnvironment()),
                moyasarConfigProperties.getBtMerchantId(),
                moyasarConfigProperties.getBtPublicKey(),
                moyasarConfigProperties.getBtPrivateKey()
        );

        try {
            // Example: Validate the base URL (indirectly tests connection)
            String baseUrl = gateway.getConfiguration().getBaseURL();

            // If no exception occurs, the connection is successful
            return HealthStatus.healthy("Moyasar OK. Base URL: " + baseUrl);
        } catch (final IllegalArgumentException e) {
            // Typically occurs for invalid environment or credentials
            logger.warn("Healthcheck error: Invalid credentials or configuration", e);
            return HealthStatus.unHealthy("Moyasar error: Invalid credentials or configuration. Details: " + e.getMessage());
        } catch (final Throwable e) {
            // Catch-all for unexpected errors
            logger.warn("Healthcheck error: General issue while connecting to Moyasar", e);
            return HealthStatus.unHealthy("Moyasar error: Unexpected issue. Details: " + e.getMessage());
        }
    }
}
