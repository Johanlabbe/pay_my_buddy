/* ===========================================================
INITIALISATION DE LA BDD pay_my_buddy
=========================================================== */

/* Rend le script ré-exécutable sans erreur */
DROP TABLE IF EXISTS invoice;

DROP TABLE IF EXISTS transaction;

DROP TABLE IF EXISTS user_connection;

DROP TABLE IF EXISTS userdb;

/* Schéma */
CREATE TABLE userdb (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    solde DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE TABLE user_connection (
    user_id BIGINT NOT NULL,
    connection_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, connection_id),
    CONSTRAINT fk_uc_user FOREIGN KEY (user_id) REFERENCES userdb (id) ON DELETE CASCADE,
    CONSTRAINT fk_uc_connection FOREIGN KEY (connection_id) REFERENCES userdb (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NULL,
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tr_sender FOREIGN KEY (sender_id) REFERENCES userdb (id),
    CONSTRAINT fk_tr_receiver FOREIGN KEY (receiver_id) REFERENCES userdb (id)
) ENGINE = InnoDB;

/* Table de factures */
CREATE TABLE invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total DECIMAL(15, 2) NOT NULL,
    issued_at DATE NOT NULL,
    CONSTRAINT fk_inv_user FOREIGN KEY (user_id) REFERENCES userdb (id)
) ENGINE = InnoDB;

/*  Jeu de données démo */
/* Utilisateurs */
INSERT INTO
    userdb (
        id,
        username,
        email,
        password,
        solde
    )
VALUES (
        1,
        'alice',
        'alice@example.com',
        '$2a$10$uTzdCeIhJJohPRUkt6JUAOM.xIm2uF7Nr/97v0yYvkRpeJ.RP6O/u',
        1000.00
    ),
    (
        2,
        'bob',
        'bob@example.com',
        '$2a$10$uTzdCeIhJJohPRUkt6JUAOM.xIm2uF7Nr/97v0yYvkRpeJ.RP6O/u',
        1000.00
    );

/* Connexions unidirectionnelles */
INSERT INTO
    user_connection (user_id, connection_id)
VALUES (1, 2),
    (2, 1);

/* Transactions */
INSERT INTO
    transaction (
        id,
        description,
        amount,
        sender_id,
        receiver_id
    )
VALUES (1, 'Payment', 100.00, 1, 2),
    (2, 'Refund', 50.00, 2, 1);