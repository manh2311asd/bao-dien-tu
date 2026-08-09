CREATE TABLE IF NOT EXISTS Users
(
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    full_name          VARCHAR(255)                                        NOT NULL,
    email              VARCHAR(255)                                        NOT NULL UNIQUE,
    password_hash      VARCHAR(255)                                        NOT NULL,
    role               ENUM ('MEMBER', 'VIP', 'AUTHOR', 'CENSOR', 'ADMIN') NOT NULL,
    vip_expiry_date    DATETIME                                            NULL,
    free_articles_left INT                       DEFAULT 3,
    status             ENUM ('ACTIVE', 'LOCKED') DEFAULT 'ACTIVE',
    created_at         DATETIME                  DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS Categories
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS Articles
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    author_id        INT                  NOT NULL,
    cover_image      VARCHAR(500)         NOT NULL,
    category_id      INT                  NOT NULL,
    title            VARCHAR(255)         NOT NULL,
    sapo             TEXT                 NOT NULL,
    content          LONGTEXT             NOT NULL,
    type             ENUM ('FREE', 'VIP') NOT NULL,
    status           ENUM ('DRAFT', 'PENDING', 'PUBLISHED', 'REJECTED', 'HIDDEN'),
    rejection_reason TEXT                 NULL,
    view_count       INT      DEFAULT 0,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (author_id) REFERENCES Users (id),
    FOREIGN KEY (category_id) REFERENCES Categories (id)
);
CREATE TABLE IF NOT EXISTS Comments
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    article_id INT  NOT NULL,
    user_id    INT  NOT NULL,
    content    TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (article_id) REFERENCES Articles (id),
    FOREIGN KEY (user_id) REFERENCES Users (id)
);
CREATE TABLE IF NOT EXISTS Subscriptions
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT                         NOT NULL,
    target_id   INT                         NOT NULL,
    target_type ENUM ('AUTHOR', 'CATEGORY') NOT NULL,

    FOREIGN KEY (user_id) REFERENCES Users (id)
);
CREATE TABLE IF NOT EXISTS Vip_Packages
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255)   NOT NULL,
    duration_days INT            NOT NULL,
    price         DECIMAL(10, 2) NOT NULL,
    discount_percent INT         NOT NULL DEFAULT 0,
    description   TEXT           NULL
);

ALTER TABLE Vip_Packages
    ADD COLUMN IF NOT EXISTS discount_percent INT NOT NULL DEFAULT 0;
CREATE TABLE IF NOT EXISTS Transactions
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT            NOT NULL,
    package_id   INT            NOT NULL,
    amount       DECIMAL(10, 2) NOT NULL,
    payment_code VARCHAR(255) UNIQUE,
    status       ENUM ('PENDING', 'SUCCESS', 'FAILED', 'CANCELED'),
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES Users (id),
    FOREIGN KEY (package_id) REFERENCES Vip_Packages (id)
);

CREATE TABLE IF NOT EXISTS Article_Views
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    article_id INT NOT NULL,
    user_id    INT NULL, -- NULL for guest reads
    viewed_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES Articles (id),
    FOREIGN KEY (user_id) REFERENCES Users (id)
);