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

package org.killbill.billing.plugin.moyasar.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.plugin.dao.payment.PluginPaymentDao;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.killbill.billing.plugin.moyasar.dao.gen.tables.MoyasarPaymentMethods;
import org.killbill.billing.plugin.moyasar.dao.gen.tables.MoyasarResponses;
import org.killbill.billing.plugin.moyasar.dao.gen.tables.records.MoyasarPaymentMethodsRecord;
import org.killbill.billing.plugin.moyasar.dao.gen.tables.records.MoyasarResponsesRecord;

import static org.killbill.billing.plugin.moyasar.dao.gen.tables.MoyasarPaymentMethods.MOYASAR_PAYMENT_METHODS;
import static org.killbill.billing.plugin.moyasar.dao.gen.tables.MoyasarResponses.MOYASAR_RESPONSES;

public class MoyasarDao extends PluginPaymentDao<MoyasarResponsesRecord, MoyasarResponses, MoyasarPaymentMethodsRecord, MoyasarPaymentMethods> {

    public MoyasarDao(final DataSource dataSource) throws SQLException {
        super(MOYASAR_RESPONSES, MOYASAR_PAYMENT_METHODS, dataSource);
        // Save space in the database
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
    }

    // Payment methods
    public void addPaymentMethod(final UUID kbAccountId,
                                 final UUID kbPaymentMethodId,
                                 final boolean isDefault,
                                 final Map<String, Object> additionalDataMap,
                                 final String moyasarToken,
                                 final DateTime utcNow,
                                 final UUID kbTenantId) throws SQLException {
        execute(dataSource.getConnection(),
                new WithConnectionCallback<MoyasarResponsesRecord>() {
                    @Override
                    public MoyasarResponsesRecord withConnection(final Connection conn) throws SQLException {
                        DSL.using(conn, dialect, settings)
                                .insertInto(MOYASAR_PAYMENT_METHODS,
                                        MOYASAR_PAYMENT_METHODS.KB_ACCOUNT_ID,
                                        MOYASAR_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID,
                                        MOYASAR_PAYMENT_METHODS.MOYASAR_ID,
                                        MOYASAR_PAYMENT_METHODS.IS_DEFAULT,
                                        MOYASAR_PAYMENT_METHODS.IS_DELETED,
                                        MOYASAR_PAYMENT_METHODS.ADDITIONAL_DATA,
                                        MOYASAR_PAYMENT_METHODS.CREATED_DATE,
                                        MOYASAR_PAYMENT_METHODS.UPDATED_DATE,
                                        MOYASAR_PAYMENT_METHODS.KB_TENANT_ID)
                                .values(kbAccountId.toString(),
                                        kbPaymentMethodId.toString(),
                                        moyasarToken,
                                        (short) (isDefault? TRUE : FALSE),
                                        (short) FALSE,
                                        String.valueOf(JSONB.valueOf(asString(additionalDataMap))),
                                        toLocalDateTime(utcNow),
                                        toLocalDateTime(utcNow),
                                        kbTenantId.toString()
                                )
                                .execute();

                        return null;
                    }
                });
    }

    public void updatePaymentMethod(final UUID kbPaymentMethodId,
                                    final Map<String, Object> additionalDataMap,
                                    final String moyasarId,
                                    final DateTime utcNow,
                                    final UUID kbTenantId) throws SQLException {
        execute(dataSource.getConnection(),
                new WithConnectionCallback<MoyasarResponsesRecord>() {
                    @Override
                    public MoyasarResponsesRecord withConnection(final Connection conn) throws SQLException {
                        DSL.using(conn, dialect, settings)
                                .update(MOYASAR_PAYMENT_METHODS)
                                .set(MOYASAR_PAYMENT_METHODS.ADDITIONAL_DATA, asString(additionalDataMap))
                                .set(MOYASAR_PAYMENT_METHODS.UPDATED_DATE, toLocalDateTime(utcNow))
                                .where(MOYASAR_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID.equal(kbPaymentMethodId.toString()))
                                .and(MOYASAR_PAYMENT_METHODS.MOYASAR_ID.equal(moyasarId))
                                .and(MOYASAR_PAYMENT_METHODS.KB_TENANT_ID.equal(kbTenantId.toString()))
                                .execute();
                        return null;
                    }

                });
    }

    // Responses

//    public MoyasarResponsesRecord addResponse(final UUID kbAccountId,
//                                                final UUID kbPaymentId,
//                                                final UUID kbPaymentTransactionId,
//                                                final TransactionType transactionType,
//                                                final BigDecimal amount,
//                                                final Currency currency,
//                                                final DateTime utcNow,
//                                                final UUID kbTenantId) throws SQLException {
//        final Map<String, Object> additionalDataMap = MoyasarPluginProperties.toAdditionalDataMap();
//
//        return execute(dataSource.getConnection(),
//                connection -> DSL.using(connection, dialect, settings).transactionResult(configuration -> {
//                    final DSLContext dslContext = DSL.using(configuration);
//                    dslContext.insertInto(MOYASAR_RESPONSES,
//                                    MOYASAR_RESPONSES.KB_ACCOUNT_ID,
//                                    MOYASAR_RESPONSES.KB_PAYMENT_ID,
//                                    MOYASAR_RESPONSES.KB_PAYMENT_TRANSACTION_ID,
//                                    MOYASAR_RESPONSES.TRANSACTION_TYPE,
//                                    MOYASAR_RESPONSES.AMOUNT,
//                                    MOYASAR_RESPONSES.CURRENCY,
//                                    MOYASAR_RESPONSES.MOYASAR_ID,
//                                    MOYASAR_RESPONSES.ADDITIONAL_DATA,
//                                    MOYASAR_RESPONSES.CREATED_DATE,
//                                    MOYASAR_RESPONSES.KB_TENANT_ID)
//                            .values(kbAccountId.toString(),
//                                    kbPaymentId.toString(),
//                                    kbPaymentTransactionId.toString(),
//                                    transactionType.toString(),
//                                    amount,
//                                    currency == null ? null : currency.name(),
//                                    MoyasarClient.getTransactionInstance(moyasarResult).getId(),
//                                    asString(additionalDataMap),
//                                    toLocalDateTime(utcNow),
//                                    kbTenantId.toString())
//                            .execute();
//                    return dslContext.fetchOne(MOYASAR_RESPONSES,
//                            MOYASAR_RESPONSES.RECORD_ID.eq(MOYASAR_RESPONSES.RECORD_ID.getDataType().convert(dslContext.lastID())));
//                }));
//    }
//
//    public MoyasarResponsesRecord updateResponse(final UUID kbPaymentTransactionId,
//                                                   final Iterable<PluginProperty> additionalPluginProperties,
//                                                   final UUID kbTenantId) throws SQLException {
//        final Map<String, Object> additionalProperties = PluginProperties.toMap(additionalPluginProperties);
//        return updateResponse(kbPaymentTransactionId, additionalProperties, kbTenantId);
//    }
//
//    public MoyasarResponsesRecord updateResponse(final UUID kbPaymentTransactionId,
//                                                   final Map<String, Object> additionalProperties,
//                                                   final UUID kbTenantId) throws SQLException {
//        return execute(dataSource.getConnection(),
//                new WithConnectionCallback<MoyasarResponsesRecord>() {
//                    @Override
//                    public MoyasarResponsesRecord withConnection(final Connection conn) throws SQLException {
//                        final MoyasarResponsesRecord response = DSL.using(conn, dialect, settings)
//                                .selectFrom(MOYASAR_RESPONSES)
//                                .where(MOYASAR_RESPONSES.KB_PAYMENT_TRANSACTION_ID.equal(kbPaymentTransactionId.toString()))
//                                .and(MOYASAR_RESPONSES.KB_TENANT_ID.equal(kbTenantId.toString()))
//                                .orderBy(MOYASAR_RESPONSES.RECORD_ID.desc())
//                                .limit(1)
//                                .fetchOne();
//
//                        if (response == null) {
//                            return null;
//                        }
//
//                        final Map originalData = new HashMap(mapFromAdditionalDataString(response.getAdditionalData()));
//                        originalData.putAll(additionalProperties);
//
//                        DSL.using(conn, dialect, settings)
//                                .update(MOYASAR_RESPONSES)
//                                .set(MOYASAR_RESPONSES.ADDITIONAL_DATA, asString(originalData))
//                                .where(MOYASAR_RESPONSES.RECORD_ID.equal(response.getRecordId()))
//                                .execute();
//                        return response;
//                    }
//                });
//    }
//
//    public void updateResponse(final MoyasarResponsesRecord moyasarResponsesRecord,
//                               final Map additionalMetadata) throws SQLException {
//        final Map additionalDataMap = mapFromAdditionalDataString(moyasarResponsesRecord.getAdditionalData());
//        additionalDataMap.putAll(additionalMetadata);
//
//        execute(dataSource.getConnection(),
//                new WithConnectionCallback<Void>() {
//                    @Override
//                    public Void withConnection(final Connection conn) throws SQLException {
//                        DSL.using(conn, dialect, settings)
//                                .update(MOYASAR_RESPONSES)
//                                .set(MOYASAR_RESPONSES.ADDITIONAL_DATA, asString(additionalDataMap))
//                                .where(MOYASAR_RESPONSES.RECORD_ID.equal(moyasarResponsesRecord.getRecordId()))
//                                .execute();
//                        return null;
//                    }
//                });
//    }

    @Override
    public MoyasarResponsesRecord getSuccessfulAuthorizationResponse(final UUID kbPaymentId, final UUID kbTenantId) throws SQLException {
        return execute(dataSource.getConnection(),
                new WithConnectionCallback<MoyasarResponsesRecord>() {
                    @Override
                    public MoyasarResponsesRecord withConnection(final Connection conn) throws SQLException {
                        return DSL.using(conn, dialect, settings)
                                .selectFrom(responsesTable)
                                .where(DSL.field(responsesTable.getName() + "." + KB_PAYMENT_ID).equal(kbPaymentId.toString()))
                                .and(
                                        DSL.field(responsesTable.getName() + "." + TRANSACTION_TYPE).equal(TransactionType.AUTHORIZE.toString())
                                                .or(DSL.field(responsesTable.getName() + "." + TRANSACTION_TYPE).equal(TransactionType.PURCHASE.toString()))
                                )
                                .and(DSL.field(responsesTable.getName() + "." + KB_TENANT_ID).equal(kbTenantId.toString()))
                                .orderBy(DSL.field(responsesTable.getName() + "." + RECORD_ID).desc())
                                .limit(1)
                                .fetchOne();
                    }
                });
    }


    public static Map mapFromAdditionalDataString(@Nullable final String additionalData) {
        if (additionalData == null) {
            return ImmutableMap.of();
        }

        try {
            return objectMapper.readValue(additionalData, Map.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String stringFromAdditionalDataMap(@Nullable final Map<String, Object> additionalData) {
        if (additionalData == null || additionalData.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(additionalData);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}