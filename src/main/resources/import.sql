
INSERT INTO inverter_manufacturer(id, name, portal_url) VALUES (1, 'GROWATT', 'https://server.growatt.com/?lang=pt');
INSERT INTO inverter_manufacturer(id, name, portal_url) VALUES (2, 'SOLIS', 'https://soliscloud.com/#/homepage');
INSERT INTO inverter_manufacturer(id, name, portal_url) VALUES (3, 'SUNGROW', 'https://isolarcloud.com/#/login');

INSERT INTO company(id, name_by_user, register_name, active, created_at) VALUES (1, 'LAGOM', 'LAGOM', true, NOW());
INSERT INTO company(id, name_by_user, register_name, active, created_at) VALUES (2, 'SOLARELLI', 'SOLARELLI', true, NOW());

INSERT INTO user_account(id, email, user_name, password, account_owner, company_id) VALUES (1, 'lucas@email', 'lucas_teste', 'senha123', true, 1);
INSERT INTO user_account(id, email, user_name, password, account_owner, company_id) VALUES (2, 'jose@email', 'jose_teste', 'senha123',false, 1);
INSERT INTO user_account(id, email, user_name, password, account_owner, company_id) VALUES (3, 'SOLAR@email', 'user_teste', 'senha123', true, 2);