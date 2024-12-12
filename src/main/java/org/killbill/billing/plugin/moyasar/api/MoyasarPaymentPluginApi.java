/*
 * Copyright 2021 Wovenware, Inc
 * Copyright 2020-2021 Equinix, Inc
 * Copyright 2014-2021 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.moyasar.api;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.plugin.api.*;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.payment.PluginPaymentPluginApi;
import org.killbill.billing.plugin.moyasar.client.MoyasarClient;
import org.killbill.billing.plugin.moyasar.client.MoyasarClientImpl;
import org.killbill.billing.plugin.moyasar.dao.MoyasarDao;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.plugin.dao.payment.PluginPaymentDao;
import org.killbill.billing.plugin.moyasar.dao.gen.tables.MoyasarPaymentMethods;
import org.killbill.billing.plugin.moyasar.dao.gen.tables.MoyasarResponses;
import org.killbill.billing.plugin.moyasar.dao.gen.tables.records.MoyasarPaymentMethodsRecord;
import org.killbill.billing.plugin.moyasar.dao.gen.tables.records.MoyasarResponsesRecord;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.clock.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoyasarPaymentPluginApi extends PluginPaymentPluginApi<MoyasarResponsesRecord, MoyasarResponses, MoyasarPaymentMethodsRecord, MoyasarPaymentMethods> {

    private static final Logger logger = LoggerFactory.getLogger(MoyasarPaymentPluginApi.class);
    private  MoyasarDao dao;

    private PluginPaymentDao paymentPluginDao;

    public MoyasarPaymentPluginApi(final OSGIKillbillAPI killbillAPI,
                                   final OSGIConfigPropertiesService configProperties,
                                   final Clock clock,
                                   final MoyasarDao dao) {
        super(killbillAPI, configProperties, clock, dao);
        this.dao = dao;
    }


    @Override
    protected PaymentTransactionInfoPlugin buildPaymentTransactionInfoPlugin(MoyasarResponsesRecord record) {
        return null;
    }

    @Override
    protected PaymentMethodPlugin buildPaymentMethodPlugin(MoyasarPaymentMethodsRecord record) {
            return MoyasarPaymentMethodPlugin.build(record);
        }

    @Override
    protected PaymentMethodInfoPlugin buildPaymentMethodInfoPlugin(MoyasarPaymentMethodsRecord record) {
        return null;
    }

    @Override
    protected String getPaymentMethodId(MoyasarPaymentMethodsRecord input) {
        return null;
    }

    @Override
    public PaymentTransactionInfoPlugin authorizePayment(UUID kbAccountId, UUID kbPaymentId, UUID kbTransactionId, UUID kbPaymentMethodId, BigDecimal amount, Currency currency, Iterable<PluginProperty> properties, CallContext context) throws PaymentPluginApiException {
        return null;
    }
    @Override
    public void addPaymentMethod(final UUID kbAccountId,
                                 final UUID kbPaymentMethodId,
                                 final PaymentMethodPlugin paymentMethodProps,
                                 final boolean setDefault,
                                 final Iterable<PluginProperty> properties,
                                 final CallContext context) throws PaymentPluginApiException {


        final Iterable<PluginProperty> allProperties = PluginProperties.merge(paymentMethodProps.getProperties(), properties);
        String name = PluginProperties.findPluginPropertyValue("ccFirstName" , allProperties);
        String Card_Number = PluginProperties.findPluginPropertyValue("ccNumber" , allProperties);
        String month = PluginProperties.findPluginPropertyValue("ccExpirationMonth" , allProperties);
        String year = PluginProperties.findPluginPropertyValue("ccExpirationYear" , allProperties);
        String cvc = PluginProperties.findPluginPropertyValue("zip" , allProperties);
        MoyasarClient moyasarClient = new MoyasarClientImpl();
        String token = moyasarClient.createPaymentMethod(name ,Card_Number , month , year , cvc);
        final DateTime utcNow = clock.getUTCNow();
        try {
            Map data = moyasarClient.getPaymentMethod(token);
            dao.addPaymentMethod(kbAccountId , kbPaymentMethodId , setDefault , data, token ,utcNow , context.getTenantId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public PaymentMethodPlugin getPaymentMethodDetail(UUID kbAccountId, UUID kbPaymentMethodId,
                                                      Iterable<PluginProperty> properties, TenantContext context) throws PaymentPluginApiException {
        final MoyasarPaymentMethodsRecord record;
        try {
            record = dao.getPaymentMethod(kbPaymentMethodId, context.getTenantId());
        } catch (final SQLException e) {
            throw new PaymentPluginApiException("Unable to retrieve payment method for kbPaymentMethodId " + kbPaymentMethodId, e);
        }

        if (record == null) {
            // Known in KB but deleted in Braintree?
            return new MoyasarPaymentMethodPlugin(kbPaymentMethodId,
                    null,
                    false,
                    ImmutableList.<PluginProperty>of());
        } else {
            return buildPaymentMethodPlugin(record);
        }
    }
    @Override
    public List<PaymentMethodInfoPlugin> getPaymentMethods(final UUID kbAccountId,
                                                           final boolean refreshFromGateway,
                                                           final Iterable<PluginProperty> properties,
                                                           final CallContext context) throws PaymentPluginApiException {
            return super.getPaymentMethods(kbAccountId, refreshFromGateway, properties, context);

    }
    @Override
    public PaymentTransactionInfoPlugin capturePayment(UUID kbAccountId, UUID kbPaymentId, UUID kbTransactionId, UUID kbPaymentMethodId, BigDecimal amount, Currency currency, Iterable<PluginProperty> properties, CallContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public PaymentTransactionInfoPlugin purchasePayment(UUID kbAccountId, UUID kbPaymentId, UUID kbTransactionId, UUID kbPaymentMethodId, BigDecimal amount, Currency currency, Iterable<PluginProperty> properties, CallContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public PaymentTransactionInfoPlugin voidPayment(UUID kbAccountId, UUID kbPaymentId, UUID kbTransactionId, UUID kbPaymentMethodId, Iterable<PluginProperty> properties, CallContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public PaymentTransactionInfoPlugin creditPayment(UUID kbAccountId, UUID kbPaymentId, UUID kbTransactionId, UUID kbPaymentMethodId, BigDecimal amount, Currency currency, Iterable<PluginProperty> properties, CallContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public PaymentTransactionInfoPlugin refundPayment(UUID kbAccountId, UUID kbPaymentId, UUID kbTransactionId, UUID kbPaymentMethodId, BigDecimal amount, Currency currency, Iterable<PluginProperty> properties, CallContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public HostedPaymentPageFormDescriptor buildFormDescriptor(UUID kbAccountId, Iterable<PluginProperty> customFields, Iterable<PluginProperty> properties, CallContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public GatewayNotification processNotification(String notification, Iterable<PluginProperty> properties, CallContext context) throws PaymentPluginApiException {
        return null;
    }

}