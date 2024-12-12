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

package org.killbill.billing.plugin.moyasar.core.resources;

import com.google.common.collect.Iterables;
import org.joda.time.DateTime;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.moyasar.api.MoyasarPaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.moyasar.core.MoyasarConfigProperties;
import org.killbill.billing.plugin.moyasar.core.MoyasarPluginProperties;
import org.killbill.billing.plugin.moyasar.dao.MoyasarDao;
import org.killbill.clock.Clock;

import java.util.List;
import java.util.Map;

public class ExpiredPaymentPolicy {

    private final Clock clock;

    private final MoyasarConfigProperties moyasarProperties;

    public ExpiredPaymentPolicy(final Clock clock, final MoyasarConfigProperties moyasarProperties) {
        this.clock = clock;
        this.moyasarProperties = moyasarProperties;
    }

    public MoyasarPaymentTransactionInfoPlugin isExpired(final List<PaymentTransactionInfoPlugin> paymentTransactions) {
        if (!containOnlyAuthsOrPurchases(paymentTransactions)) {
            return null;
        }

        final MoyasarPaymentTransactionInfoPlugin transaction = (MoyasarPaymentTransactionInfoPlugin) latestTransaction(paymentTransactions);
        if (transaction.getCreatedDate() == null) {
            return null;
        }

        if (transaction.getStatus() == PaymentPluginStatus.PENDING) {
            final DateTime expirationDate = expirationDateForInitialTransactionType(transaction);
            if (clock.getNow(expirationDate.getZone()).isAfter(expirationDate)) {
                return transaction;
            }
        }

        return null;
    }

    private PaymentTransactionInfoPlugin latestTransaction(final List<PaymentTransactionInfoPlugin> paymentTransactions) {
        return Iterables.getLast(paymentTransactions);
    }

    private boolean containOnlyAuthsOrPurchases(final List<PaymentTransactionInfoPlugin> transactions) {
        for (final PaymentTransactionInfoPlugin transaction : transactions) {
            if (transaction.getTransactionType() != TransactionType.AUTHORIZE &&
                transaction.getTransactionType() != TransactionType.PURCHASE) {
                return false;
            }
        }
        return true;
    }

    private DateTime expirationDateForInitialTransactionType(final MoyasarPaymentTransactionInfoPlugin transaction) {
        if (transaction.getMoyasarResponsesRecord() == null) {
            return transaction.getCreatedDate().plus(moyasarProperties.getPendingPaymentExpirationPeriod(null));
        }

        final Map moyasarResponseAdditionalData = MoyasarDao.mapFromAdditionalDataString(transaction.getMoyasarResponsesRecord().getAdditionalData());

        final String paymentMethod = getPaymentMethod(moyasarResponseAdditionalData);
        return transaction.getCreatedDate().plus(moyasarProperties.getPendingPaymentExpirationPeriod(paymentMethod));
    }

    private String getPaymentMethod(final Map moyasarResponseAdditionalData) {
        return (String) moyasarResponseAdditionalData.get(MoyasarPluginProperties.PROPERTY_MS_PAYMENT_INSTRUMENT_TYPE);
    }
}