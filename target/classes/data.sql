-- Users
INSERT INTO User(id, username, email, password, solde) VALUES (1, 'alice', 'alice@example.com', 'pass', 1000.00);
INSERT INTO User(id, username, email, password, solde) VALUES (2, 'bob', 'bob@example.com', 'pass', 1000.00);

-- User connections (unidirectionnelles)
INSERT INTO UserConnection(user_id, connection_id) VALUES (1, 2);
INSERT INTO UserConnection(user_id, connection_id) VALUES (2, 1);

-- Alice envoie à Bob --> Ok car (1,2) existe
INSERT INTO Transaction(id, description, amount, sender_id, receiver_id) VALUES (1, 'Payment', 100.00, 1, 2);

-- Bob envoie à Alice --> Ok car (2,1) existe
INSERT INTO Transaction(id, description, amount, sender_id, receiver_id) VALUES (2, 'Refund', 50.00, 2, 1);
