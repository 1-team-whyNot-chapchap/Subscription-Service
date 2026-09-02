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
-- DUPLICATE KEY(중복키) 중복값 입력하려고 할 때 에러발생하지 않고, 값 업데이트 처리
-- code에 code 값 업데이트

INSERT INTO terms (
    terms_type,
    version_number,
    title,
    content,
    is_required,
    is_current
)
SELECT
    'NON_FACE_TO_FACE_STORAGE',
    1,
    '비대면 보관 약관',
    '챱챱은 고객이 지정한 배송지와 배달 요청사항에 따라 구독 상품을 배송합니다.

1. 고객이 배송 시 상품을 직접 수령하지 못하는 경우, 고객이 배송지에 설정한 배달 방식과 요청사항에 따라 상품이 문 앞 또는 고객이 지정한 장소에 보관될 수 있습니다.

2. 고객은 배송지마다 다음 배달 방식 중 하나를 선택합니다.
- 직접 받을게요
- 문 앞 배송
- 기타

기타를 선택한 경우 고객이 입력한 요청사항을 해당 배송지의 배달 요청사항으로 사용합니다.

3. 구독 주문은 주문이 생성될 당시 확정된 배송지와 배달 요청사항을 기준으로 처리됩니다.

4. 본 약관은 첫 구독 신청을 완료하고 첫 결제를 진행하기 전에 반드시 동의해야 하는 필수 약관입니다. 본 약관에 동의하지 않은 경우 첫 구독 신청 및 결제를 진행할 수 없습니다.

5. 고객의 동의 사실은 동의한 약관의 버전 및 동의 시각과 함께 기록됩니다. 동일한 약관 버전에 다시 동의하더라도 기존 동의 기록이 유지됩니다.

6. 구독을 통해 생성되는 주문은 해당 고객이 동의한 비대면 보관 약관 기록을 기준으로 생성됩니다.

본인은 위 내용을 확인하였으며, 배송 과정에서 직접 수령하지 못하는 경우 선택한 배달 방식 및 요청사항에 따라 상품이 문 앞 또는 지정한 장소에 보관될 수 있음에 동의합니다.',
    TRUE,
    TRUE
    WHERE NOT EXISTS (
    SELECT 1
    FROM terms
    WHERE terms_type = 'NON_FACE_TO_FACE_STORAGE'
      AND version_number = 1
);