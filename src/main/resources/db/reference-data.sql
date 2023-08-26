insert into year
(code,   name,        start_date,   end_date,     questions_ship_starting, maximum_packet_practice_material_price) values
('2020', '2019–2020', '2019-08-01', '2020-08-01', '2019-12-15',            75.00),
('2021', '2020–2021', '2020-08-01', '2021-08-01', '2020-12-15',            75.00),
('2022', '2021–2022', '2021-08-01', '2022-08-01', '2021-12-15',            75.00),
('2023', '2022–2023', '2022-08-01', '2023-08-01', '2022-12-15',            75.00),
('2024', '2023–2024', '2023-08-01', '2024-08-01', '2023-12-15',            null),
('2025', '2024–2025', '2024-08-01', '2025-08-01', '2024-12-15',            null);

insert into state_series
(name,                       description, price, available, sequence) values
('2023 IESA State Series',   null,        75.00, true,      -2023);

insert into packet
(year_code, number, available_for_competition, price_as_practice_material) values
('2020',    1,      false,                     5.00),
('2020',    2,      false,                     5.00),
('2020',    3,      false,                     5.00),
('2020',    4,      false,                     5.00),
('2020',    5,      false,                     5.00),
('2020',    6,      false,                     5.00),
('2020',    7,      false,                     5.00),
('2020',    8,      false,                     5.00),
('2020',    9,      false,                     5.00),
('2020',    10,     false,                     5.00),
('2020',    11,     false,                     5.00),
('2020',    12,     false,                     5.00),
('2020',    13,     false,                     5.00),
('2020',    14,     false,                     5.00),
('2020',    15,     false,                     5.00),
('2021',    1,      false,                     5.00),
('2021',    2,      false,                     5.00),
('2021',    3,      false,                     5.00),
('2021',    4,      false,                     5.00),
('2021',    5,      false,                     5.00),
('2021',    6,      false,                     5.00),
('2021',    7,      false,                     5.00),
('2021',    8,      false,                     5.00),
('2021',    9,      false,                     5.00),
('2021',    10,     false,                     5.00),
('2021',    11,     false,                     5.00),
('2021',    12,     false,                     5.00),
('2021',    13,     false,                     5.00),
('2021',    14,     false,                     5.00),
('2021',    15,     false,                     5.00),
('2022',    1,      false,                     5.00),
('2022',    2,      false,                     5.00),
('2022',    3,      false,                     5.00),
('2022',    4,      false,                     5.00),
('2022',    5,      false,                     5.00),
('2022',    6,      false,                     5.00),
('2022',    7,      false,                     5.00),
('2022',    8,      false,                     5.00),
('2022',    9,      false,                     5.00),
('2022',    10,     false,                     5.00),
('2022',    11,     false,                     5.00),
('2022',    12,     false,                     5.00),
('2022',    13,     false,                     5.00),
('2022',    14,     false,                     5.00),
('2022',    15,     false,                     5.00),
('2023',    1,      false,                     5.00),
('2023',    2,      false,                     5.00),
('2023',    3,      false,                     5.00),
('2023',    4,      false,                     5.00),
('2023',    5,      false,                     5.00),
('2023',    6,      false,                     5.00),
('2023',    7,      false,                     5.00),
('2023',    8,      false,                     5.00),
('2023',    9,      false,                     5.00),
('2023',    10,     false,                     5.00),
('2023',    11,     false,                     5.00),
('2023',    12,     false,                     5.00),
('2023',    13,     false,                     5.00),
('2023',    14,     false,                     5.00),
('2023',    15,     false,                     5.00),
('2024',    1,      true,                      null),
('2024',    2,      true,                      null),
('2024',    3,      true,                      null),
('2024',    4,      true,                      null),
('2024',    5,      true,                      null),
('2024',    6,      true,                      null),
('2024',    7,      true,                      null),
('2024',    8,      true,                      null),
('2024',    9,      true,                      null),
('2024',    10,     true,                      null),
('2024',    11,     true,                      null),
('2024',    12,     true,                      null),
('2024',    13,     true,                      null),
('2024',    14,     true,                      null),
('2024',    15,     true,                      null),
('2024',    16,     true,                      null),
('2024',    17,     true,                      null),
('2024',    18,     true,                      null),
('2024',    19,     true,                      null),
('2024',    20,     true,                      null);

insert into compilation
(name,                       description,                price, available, sequence) values
('Fine Arts',                '100 tossups, 85 bonuses',  25.00, true,      100),
('Language Arts/Literature', '230 tossups, 175 bonuses', 45.00, true,      200),
('Mathematics',              '225 tossups, 175 bonuses', 45.00, true,      300),
('Miscellaneous',            '100 tossups, 90 bonuses',  25.00, true,      400),
('Science',                  '230 tossups, 170 bonuses', 45.00, true,      500),
('Social Studies',           '270 tossups, 175 bonuses', 45.00, true,      600);

insert into booking_status
(code,          label,         assume_packet_exposure, sequence) values
('unsubmitted', 'Unsubmitted', false,                  100),
('submitted',   'Submitted',   true,                   200),
('approved',    'Approved',    true,                   300),
('shipped',     'Shipped',     true,                   400),
('canceled',    'Canceled',    false,                  1000),
('abandoned',   'Abandoned',   false,                  1100),
('rejected',    'Rejected',    false,                  1200);

-- school data are in a separate file
