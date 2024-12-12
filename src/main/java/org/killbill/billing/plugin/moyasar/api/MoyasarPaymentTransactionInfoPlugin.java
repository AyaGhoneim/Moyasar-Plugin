/*
 * Copyright 2021 Wovenware, Inc
 *
 * Wovenware licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.moyasar.api;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.braintreegateway.Transaction;
import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.payment.PluginPaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.moyasar.core.MoyasarPluginProperties;
import org.killbill.billing.plugin.moyasar.dao.MoyasarDao;
import org.killbill.billing.plugin.moyasar.dao.gen.tables.records.MoyasarResponsesRecord;

import javax.annotation.Nullable;

public class MoyasarPaymentTransactionInfoPlugin extends PluginPaymentTransactionInfoPlugin {

    // Kill Bill limits the field size to 32
    private static final int ERROR_CODE_MAX_LENGTH = 32;

    private final MoyasarResponsesRecord moyasarResponsesRecord;

    public static MoyasarPaymentTransactionInfoPlugin build(final MoyasarResponsesRecord moyasarResponsesRecord) {
        final Map additionalData = MoyasarDao.mapFromAdditionalDataString(moyasarResponsesRecord.getAdditionalData());

        final DateTime responseDate = new DateTime(moyasarResponsesRecord.getCreatedDate()
                .atZone(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(), DateTimeZone.UTC);
        return new MoyasarPaymentTransactionInfoPlugin(moyasarResponsesRecord,
                UUID.fromString(moyasarResponsesRecord.getKbPaymentId()),
                UUID.fromString(moyasarResponsesRecord.getKbPaymentTransactionId()),
                TransactionType.valueOf(moyasarResponsesRecord.getTransactionType()),
                moyasarResponsesRecord.getAmount(),
                Strings.isNullOrEmpty(moyasarResponsesRecord.getCurrency()) ? null : Currency.valueOf(moyasarResponsesRecord.getCurrency()),
                getPaymentPluginStatus(additionalData),
                getGatewayError(additionalData),
                truncate(getGatewayErrorCode(additionalData)),
                getFirstPaymentReferenceID(additionalData),
                getSecondPaymentReferenceID(additionalData),
                responseDate,
                responseDate,
                PluginProperties.buildPluginProperties(additionalData));
    }

    public MoyasarPaymentTransactionInfoPlugin(final MoyasarResponsesRecord moyasarResponsesRecord,
                                               final UUID kbPaymentId, final UUID kbTransactionPaymentPaymentId,
                                               final TransactionType transactionType, final BigDecimal amount, final Currency currency,
                                               final PaymentPluginStatus pluginStatus, final String gatewayError, final String gatewayErrorCode,
                                               final String firstPaymentReferenceId, final String secondPaymentReferenceId, final DateTime createdDate,
                                               final DateTime effectiveDate, final List<PluginProperty> properties) {
        super(kbPaymentId, kbTransactionPaymentPaymentId, transactionType, amount, currency, pluginStatus, gatewayError,
                gatewayErrorCode, firstPaymentReferenceId, secondPaymentReferenceId, createdDate, effectiveDate, properties);
        this.moyasarResponsesRecord = moyasarResponsesRecord;
    }

    public MoyasarResponsesRecord getMoyasarResponsesRecord() {
        return moyasarResponsesRecord;
    }

    public static PaymentPluginStatus getPaymentPluginStatus(final String moyasarStatus){
        if(Transaction.Status.SETTLED.toString().equals(moyasarStatus)
                || Transaction.Status.AUTHORIZING.toString().equals(moyasarStatus)
                || Transaction.Status.AUTHORIZED.toString().equals(moyasarStatus)
                || Transaction.Status.SETTLING.toString().equals(moyasarStatus)
                || Transaction.Status.SETTLEMENT_CONFIRMED.toString().equals(moyasarStatus)
                || Transaction.Status.SUBMITTED_FOR_SETTLEMENT.toString().equals(moyasarStatus)
                || Transaction.Status.VOIDED.toString().equals(moyasarStatus)){
            return PaymentPluginStatus.PROCESSED;
        }
        else if(Transaction.Status.SETTLEMENT_PENDING.toString().equals(moyasarStatus)){
            return PaymentPluginStatus.PENDING;
        }
        else if(Transaction.Status.FAILED.toString().equals(moyasarStatus)
                || Transaction.Status.SETTLEMENT_DECLINED.toString().equals(moyasarStatus)
                || Transaction.Status.AUTHORIZATION_EXPIRED.toString().equals(moyasarStatus)
                || Transaction.Status.PROCESSOR_DECLINED.toString().equals(moyasarStatus)
                || Transaction.Status.GATEWAY_REJECTED.toString().equals(moyasarStatus)){
            return PaymentPluginStatus.ERROR;
        }

        return PaymentPluginStatus.UNDEFINED;
    }

    public static boolean isDoneProcessingInMoyasar(final String moyasarTransactionStatus){
        return moyasarTransactionStatus.equals(Transaction.Status.SETTLED.toString())
                || moyasarTransactionStatus.equals(Transaction.Status.VOIDED.toString());
    }

    private static PaymentPluginStatus getPaymentPluginStatus(final Map additionalData) {
        final String moyasarStatus = (String) additionalData.get(MoyasarPluginProperties.PROPERTY_MS_TRANSACTION_STATUS);
        return getPaymentPluginStatus(moyasarStatus);
    }

    private static String getGatewayError(final Map additionalData) {
        return (String) additionalData.get(MoyasarPluginProperties.PROPERTY_MS_GATEWAY_ERROR_MESSAGE);
    }

    private static String getGatewayErrorCode(final Map additionalData) {
        return (String) additionalData.get(MoyasarPluginProperties.PROPERTY_MS_GATEWAY_ERROR_CODE);
    }

    private static String getFirstPaymentReferenceID(final Map additionalData){
        return (String) additionalData.get(MoyasarPluginProperties.PROPERTY_MS_FIRST_PAYMENT_REFERENCE_ID);
    }

    private static String getSecondPaymentReferenceID(final Map additionalData){
        return (String) additionalData.get(MoyasarPluginProperties.PROPERTY_MS_SECOND_PAYMENT_REFERENCE_ID);
    }

    private static String truncate(@Nullable final String string) {
        if (string == null) {
            return null;
        } else if (string.length() <= ERROR_CODE_MAX_LENGTH) {
            return string;
        } else {
            return string.substring(0, ERROR_CODE_MAX_LENGTH);
        }
    }
}
