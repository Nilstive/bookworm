-- V3__alter_currency_columns_to_varchar.sql
-- Change all CHAR(3) currency columns to VARCHAR(3) so Hibernate validation passes.

ALTER TABLE books               ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE carts               ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE orders              ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE payment_transactions ALTER COLUMN currency TYPE VARCHAR(3);
