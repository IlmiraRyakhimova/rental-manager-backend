--liquibase formatted sql

--changeset claude:003-1 splitStatements:false
-- Выравнивание схемы guests с JPA-маппингом (entity Guest использует name / phone_number / email).
-- Старые колонки из миграции 001 (main_guest_name и т.д.) переименовываются,
-- а если правильные колонки уже созданы Hibernate (ddl-auto: update) — данные переносятся.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'guests' AND column_name = 'main_guest_name') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = 'guests' AND column_name = 'name') THEN
            ALTER TABLE guests RENAME COLUMN main_guest_name TO name;
        ELSE
            UPDATE guests SET name = COALESCE(name, main_guest_name);
            ALTER TABLE guests DROP COLUMN main_guest_name;
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'guests' AND column_name = 'guest_phone_number') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = 'guests' AND column_name = 'phone_number') THEN
            ALTER TABLE guests RENAME COLUMN guest_phone_number TO phone_number;
        ELSE
            UPDATE guests SET phone_number = COALESCE(phone_number, guest_phone_number);
            ALTER TABLE guests DROP COLUMN guest_phone_number;
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'guests' AND column_name = 'guest_email') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = 'guests' AND column_name = 'email') THEN
            ALTER TABLE guests RENAME COLUMN guest_email TO email;
        ELSE
            UPDATE guests SET email = COALESCE(email, guest_email);
            ALTER TABLE guests DROP COLUMN guest_email;
        END IF;
    END IF;
END $$;

--changeset claude:003-2
-- Паспортные данные гостя для автозаполнения договора аренды
ALTER TABLE guests ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE guests ADD COLUMN IF NOT EXISTS nationality VARCHAR(255);
ALTER TABLE guests ADD COLUMN IF NOT EXISTS passport_number VARCHAR(100);
ALTER TABLE guests ADD COLUMN IF NOT EXISTS passport_issue_date DATE;

--changeset claude:003-3
-- Условия договора аренды на уровне бронирования
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS security_deposit DECIMAL(10, 2);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS security_deposit_currency VARCHAR(10);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS deposit_due_date DATE;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS cleaning_fee DECIMAL(10, 2);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS electricity_rate DECIMAL(10, 2);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS water_rate DECIMAL(10, 2);

--changeset claude:003-4
-- Уникальность токенов (entity объявляет unique = true, в миграции 001 ограничения не было)
CREATE UNIQUE INDEX IF NOT EXISTS idx_email_verification_tokens_token ON email_verification_tokens(token);
CREATE UNIQUE INDEX IF NOT EXISTS idx_password_reset_tokens_token ON password_reset_tokens(token);
