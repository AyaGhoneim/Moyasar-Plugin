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

import org.killbill.billing.plugin.api.payment.PluginPaymentMethodInfoPlugin;
import org.killbill.billing.plugin.moyasar.dao.MoyasarDao;
import org.killbill.billing.plugin.moyasar.dao.gen.tables.records.MoyasarPaymentMethodsRecord;

import java.util.UUID;

public class MoyasarPaymentMethodInfoPlugin extends PluginPaymentMethodInfoPlugin {

    public static MoyasarPaymentMethodInfoPlugin build(final MoyasarPaymentMethodsRecord moyasarPaymentMethodsRecord) {
        return new MoyasarPaymentMethodInfoPlugin(UUID.fromString(moyasarPaymentMethodsRecord.getKbAccountId()),
                UUID.fromString(moyasarPaymentMethodsRecord.getKbPaymentMethodId()),
                moyasarPaymentMethodsRecord.getIsDefault() == MoyasarDao.TRUE,
                moyasarPaymentMethodsRecord.getMoyasarId());
    }

    public MoyasarPaymentMethodInfoPlugin(final UUID accountId,
                                          final UUID paymentMethodId,
                                          final boolean isDefault,
                                          final String externalPaymentMethodId) {
        super(accountId, paymentMethodId, isDefault, externalPaymentMethodId);
    }

}
