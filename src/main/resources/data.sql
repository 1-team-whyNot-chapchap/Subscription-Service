INSERT INTO delivery_methods (
    code,
    display_name
)
VALUES
    ('DIRECT', '직접 받을게요'),
    ('DOORSTEP', '문 앞 배송'),
    ('OTHER', '기타')
    ON DUPLICATE KEY UPDATE
                         code = code;