/*
 * Copyright 2021 Wovenware, Inc
 *
 * Wovenware licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may omsain a copy of the License at:
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

import com.braintreegateway.CreditCard;
import com.braintreegateway.PayPalAccount;
import com.braintreegateway.PaymentMethod;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.UsBankAccount;

import org.killbill.billing.plugin.moyasar.client.MoyasarClient;

import java.util.HashMap;
import java.util.Map;

public abstract class MoyasarPluginProperties {
    public static final String PROPERTY_FALLBACK_VALUE = "NULL";

    public static final String PROPERTY_AMOUNT = "amount";
    public static final String PROPERTY_CURRENCY = "currency";
    public static final String PROPERTY_PAYMENT_METHOD_TYPE = "payment_method_type";

    public static final String MAGIC_FIELD_MS_CUSTOMER_ID = "MOYASAR_CUSTOMER_ID";

    public static final String PROPERTY_MS_CUSTOMER_ID = "ms_customer_id";
    public static final String PROPERTY_MS_NONCE = "ms_nonce";
    public static final String PROPERTY_MS_PAYMENT_INSTRUMENT_TYPE = "ms_payment_instrument_type";
    public static final String PROPERTY_MS_TRANSACTION_STATUS = "ms_transaction_status";
    public static final String PROPERTY_MS_TRANSACTION_SUCCESS = "ms_transaction_success";
    public static final String PROPERTY_MS_GATEWAY_ERROR_MESSAGE = "ms_gateway_error_message";
    public static final String PROPERTY_MS_GATEWAY_ERROR_CODE = "ms_gateway_error_code";
    public static final String PROPERTY_MS_FIRST_PAYMENT_REFERENCE_ID = "ms_first_payment_reference_id";
    public static final String PROPERTY_MS_SECOND_PAYMENT_REFERENCE_ID = "ms_second_payment_reference_id";

    public static final String PROPERTY_KB_TRANSACTION_ID = "kb_transaction_id";
    public static final String PROPERTY_KB_PAYMENT_ID = "kb_payment_id";
    public static final String PROPERTY_KB_TRANSACTION_TYPE = "kb_transaction_type";

    public static final String PROPERTY_OVERRIDDEN_TRANSACTION_STATUS = "overriddenTransactionStatus";
    
}
