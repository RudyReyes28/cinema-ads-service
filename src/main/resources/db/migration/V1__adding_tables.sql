CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE ad_type AS ENUM (
  'TEXT',
  'TEXT_IMAGE',
  'VIDEO_TEXT'
);

CREATE TYPE ad_period AS ENUM (
  'ONE_DAY',
  'THREE_DAYS',
  'ONE_WEEK',
  'TWO_WEEKS'
);

CREATE TYPE ad_tx_type AS ENUM ('RECHARGE', 'PAYMENT');

CREATE TABLE advertisers (
                             id         UUID         PRIMARY KEY,
                             name       VARCHAR(255) NOT NULL,
                             created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
                             updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE advertiser_wallets (
                                    id            UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
                                    advertiser_id UUID          NOT NULL UNIQUE REFERENCES advertisers(id),
                                    balance       NUMERIC(10,2) NOT NULL DEFAULT 0.00
                                        CONSTRAINT chk_adv_balance CHECK (balance >= 0),
                                    updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE advertiser_wallet_transactions (
                                                id                   UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
                                                advertiser_wallet_id UUID          NOT NULL REFERENCES advertiser_wallets(id),
                                                amount               NUMERIC(10,2) NOT NULL CHECK (amount > 0),
                                                type                 ad_tx_type    NOT NULL,
                                                description          VARCHAR(255),
                                                transaction_date     TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE ad_prices (
                           id            UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
                           ad_type       ad_type       NOT NULL,
                           period        ad_period     NOT NULL,
                           price         NUMERIC(10,2) NOT NULL CHECK (price >= 0),
                           updated_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
                           updated_by    UUID          NOT NULL,
                           UNIQUE (ad_type, period)
);

CREATE TABLE ads (
                     id            UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
                     advertiser_id UUID          NOT NULL REFERENCES advertisers(id),
                     type          ad_type       NOT NULL,
                     period        ad_period     NOT NULL,
                     content_text  TEXT          NOT NULL,
                     image_url     VARCHAR(500),
                     video_url     VARCHAR(500),
                     amount_paid   NUMERIC(10,2) NOT NULL CHECK (amount_paid >= 0),
                     start_date    TIMESTAMP     NOT NULL,
                     end_date      TIMESTAMP     NOT NULL,
                     active        BOOLEAN       NOT NULL DEFAULT TRUE,
                     created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
                     updated_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
                     CONSTRAINT chk_ads_dates CHECK (end_date > start_date)
);

CREATE INDEX idx_ads_advertiser  ON ads(advertiser_id);
CREATE INDEX idx_ads_active      ON ads(active);
CREATE INDEX idx_ads_dates       ON ads(start_date, end_date);
CREATE INDEX idx_ads_type        ON ads(type);
CREATE INDEX idx_adv_wallet      ON advertiser_wallets(advertiser_id);
CREATE INDEX idx_adv_wallet_tx   ON advertiser_wallet_transactions(advertiser_wallet_id);
CREATE INDEX idx_adv_wallet_date ON advertiser_wallet_transactions(transaction_date);