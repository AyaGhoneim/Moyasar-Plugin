/*
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
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

/*! SET default_storage_engine=INNODB */;

create table killbill.moyasar_responses (
  record_id serial
, kb_account_id char(36) not null
, kb_payment_id char(36) not null
, kb_payment_transaction_id char(36) not null
, transaction_type varchar(32) not null
, amount numeric(15,9)
, currency char(3)
, moyasar_id varchar(255) not null
, additional_data longtext default null
, created_date datetime not null
, kb_tenant_id char(36) not null
, primary key(record_id)
) /*! CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;
create index moyasar_responses_kb_payment_id on killbill.moyasar_responses(kb_payment_id);\g
create index moyasar_responses_kb_payment_transaction_id on killbill.moyasar_responses(kb_payment_transaction_id);\g
create index moyasar_responses_moyasar_id on killbill.moyasar_responses(moyasar_id);\g

create table killbill.moyasar_payment_methods (
  record_id serial
, kb_account_id char(36) not null
, kb_payment_method_id char(36) not null
, moyasar_id varchar(255) not null
, is_default smallint not null default 0
, is_deleted smallint not null default 0
, additional_data longtext default null
, created_date datetime not null
, updated_date datetime not null
, kb_tenant_id char(36) not null
, primary key(record_id)
) /*! CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;
create unique index moyasar_payment_methods_kb_payment_id on moyasar_payment_methods(kb_payment_method_id);
create index moyasar_payment_methods_moyasar_id on moyasar_payment_methods(moyasar_id);
