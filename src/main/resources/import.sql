-- Table creation is handled by Hibernate based on entity annotations
INSERT INTO inverter_manufacturer(id, name, portal_url) VALUES (1, 'GROWATT', 'https://server.growatt.com/?lang=pt');
INSERT INTO inverter_manufacturer(id, name, portal_url) VALUES (2, 'SOLIS', 'https://soliscloud.com/#/homepage');
INSERT INTO inverter_manufacturer(id, name, portal_url) VALUES (3, 'SUNGROW', 'https://isolarcloud.com/#/login');


INSERT INTO user_account(id, email, userName, password) VALUES (1, 'lucas@email', 'lucas_teste', 'senha123');
INSERT INTO user_account(id, email, userName, password) VALUES (2, 'jose@email', 'jose_teste', 'senha123');