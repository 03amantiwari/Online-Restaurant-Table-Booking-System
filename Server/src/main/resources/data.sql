INSERT IGNORE INTO roles (role_name, description) VALUES
    ('ROLE_ADMIN',    'Full system access — manages users and platform'),
    ('ROLE_OWNER',    'Restaurant owner — manages own restaurant, tables, bookings'),
    ('ROLE_CUSTOMER', 'End customer — can browse restaurants and make bookings');