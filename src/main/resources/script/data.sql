-- 1) Brand
INSERT INTO tb_brand (brand_code, name, status, created_by, updated_by)
VALUES
    ('BR-001', '브랜드A', 'ACTIVE', 'tester', 'tester'),
    ('BR-002', '브랜드B', 'ACTIVE', 'tester', 'tester')
    ON CONFLICT (brand_code) DO NOTHING;

-- 2) Product
INSERT INTO tb_product (brand_id, item_code, item_name, status, created_by, updated_by)
SELECT b.brand_id, 'ITEM-001', '상품-1', 'ACTIVE', 'tester', 'tester'
FROM tb_brand b WHERE b.brand_code = 'BR-001'
    ON CONFLICT (item_code) DO NOTHING;

INSERT INTO tb_product (brand_id, item_code, item_name, status, created_by, updated_by)
SELECT b.brand_id, 'ITEM-002', '상품-2', 'ACTIVE', 'tester', 'tester'
FROM tb_brand b WHERE b.brand_code = 'BR-001'
    ON CONFLICT (item_code) DO NOTHING;

INSERT INTO tb_product (brand_id, item_code, item_name, status, created_by, updated_by)
SELECT b.brand_id, 'ITEM-101', '상품-101', 'ACTIVE', 'tester', 'tester'
FROM tb_brand b WHERE b.brand_code = 'BR-002'
    ON CONFLICT (item_code) DO NOTHING;

-- 3) Orders
-- 2026-02-27 COMPLETED 2건 (정산 대상)
INSERT INTO tb_order (
    order_no, brand_id, product_id, item_code, item_name,
    qty, unit_price, amount, status,
    ordered_at, status_changed_at, settled_at,
    created_by, updated_by
)
SELECT
    'ORD-20260227-0001',
    b.brand_id,
    p.product_id,
    p.item_code,
    p.item_name,
    1,
    15000.00,
    15000.00,
    'COMPLETED',
    '2026-02-27T16:00:00+09:00',
    '2026-02-27T16:30:00+09:00',
    NULL,
    'tester', 'tester'
FROM tb_brand b
JOIN tb_product p
  ON p.brand_id = b.brand_id
 AND p.item_code = 'ITEM-002'
WHERE b.brand_code = 'BR-001'
    ON CONFLICT (order_no) DO NOTHING;

INSERT INTO tb_order (
    order_no, brand_id, product_id, item_code, item_name,
    qty, unit_price, amount, status,
    ordered_at, status_changed_at, settled_at,
    created_by, updated_by
)
SELECT
    'ORD-20260227-0002',
    b.brand_id,
    p.product_id,
    p.item_code,
    p.item_name,
    1,
    7000.00,
    7000.00,
    'COMPLETED',
    '2026-02-27T18:10:00+09:00',
    '2026-02-27T18:40:00+09:00',
    NULL,
    'tester', 'tester'
FROM tb_brand b
JOIN tb_product p
  ON p.brand_id = b.brand_id
 AND p.item_code = 'ITEM-101'
WHERE b.brand_code = 'BR-002'
    ON CONFLICT (order_no) DO NOTHING;

-- 2026-03-03 COMPLETED 3건 (정산 대상)
INSERT INTO tb_order (
    order_no, brand_id, product_id, item_code, item_name,
    qty, unit_price, amount, status,
    ordered_at, status_changed_at, settled_at,
    created_by, updated_by
)
SELECT
    'ORD-20260303-0001',
    b.brand_id,
    p.product_id,
    p.item_code,
    p.item_name,
    2,
    10000.00,
    20000.00,
    'COMPLETED',
    '2026-03-03T09:00:00+09:00',
    '2026-03-03T10:00:00+09:00',
    NULL,
    'tester', 'tester'
FROM tb_brand b
JOIN tb_product p
  ON p.brand_id = b.brand_id
 AND p.item_code = 'ITEM-001'
WHERE b.brand_code = 'BR-001'
    ON CONFLICT (order_no) DO NOTHING;

INSERT INTO tb_order (
    order_no, brand_id, product_id, item_code, item_name,
    qty, unit_price, amount, status,
    ordered_at, status_changed_at, settled_at,
    created_by, updated_by
)
SELECT
    'ORD-20260303-0002',
    b.brand_id,
    p.product_id,
    p.item_code,
    p.item_name,
    1,
    15000.00,
    15000.00,
    'COMPLETED',
    '2026-03-03T11:00:00+09:00',
    '2026-03-03T12:00:00+09:00',
    NULL,
    'tester', 'tester'
FROM tb_brand b
JOIN tb_product p
  ON p.brand_id = b.brand_id
 AND p.item_code = 'ITEM-002'
WHERE b.brand_code = 'BR-001'
    ON CONFLICT (order_no) DO NOTHING;

INSERT INTO tb_order (
    order_no, brand_id, product_id, item_code, item_name,
    qty, unit_price, amount, status,
    ordered_at, status_changed_at, settled_at,
    created_by, updated_by
)
SELECT
    'ORD-20260303-0003',
    b.brand_id,
    p.product_id,
    p.item_code,
    p.item_name,
    3,
    7000.00,
    21000.00,
    'COMPLETED',
    '2026-03-03T13:00:00+09:00',
    '2026-03-03T14:00:00+09:00',
    NULL,
    'tester', 'tester'
FROM tb_brand b
JOIN tb_product p
  ON p.brand_id = b.brand_id
 AND p.item_code = 'ITEM-101'
WHERE b.brand_code = 'BR-002'
    ON CONFLICT (order_no) DO NOTHING;

-- 2026-03-03 CANCELED 1건 (정산 제외)
INSERT INTO tb_order (
    order_no, brand_id, product_id, item_code, item_name,
    qty, unit_price, amount, status,
    ordered_at, status_changed_at, settled_at,
    created_by, updated_by
)
SELECT
    'ORD-20260303-0004',
    b.brand_id,
    p.product_id,
    p.item_code,
    p.item_name,
    1,
    9900.00,
    9900.00,
    'CANCELED',
    '2026-03-03T15:00:00+09:00',
    '2026-03-03T15:10:00+09:00',
    NULL,
    'tester', 'tester'
FROM tb_brand b
JOIN tb_product p
  ON p.brand_id = b.brand_id
 AND p.item_code = 'ITEM-001'
WHERE b.brand_code = 'BR-001'
    ON CONFLICT (order_no) DO NOTHING;

-- 2026-03-04 COMPLETED 2건
INSERT INTO tb_order (
    order_no, brand_id, product_id, item_code, item_name,
    qty, unit_price, amount, status,
    ordered_at, status_changed_at, settled_at,
    created_by, updated_by
)
SELECT
    'ORD-20260304-0001',
    b.brand_id,
    p.product_id,
    p.item_code,
    p.item_name,
    1,
    10000.00,
    10000.00,
    'COMPLETED',
    '2026-03-04T09:30:00+09:00',
    '2026-03-04T10:00:00+09:00',
    NULL,
    'tester', 'tester'
FROM tb_brand b
JOIN tb_product p
  ON p.brand_id = b.brand_id
 AND p.item_code = 'ITEM-001'
WHERE b.brand_code = 'BR-001'
    ON CONFLICT (order_no) DO NOTHING;

INSERT INTO tb_order (
    order_no, brand_id, product_id, item_code, item_name,
    qty, unit_price, amount, status,
    ordered_at, status_changed_at, settled_at,
    created_by, updated_by
)
SELECT
    'ORD-20260304-0002',
    b.brand_id,
    p.product_id,
    p.item_code,
    p.item_name,
    2,
    7000.00,
    14000.00,
    'COMPLETED',
    '2026-03-04T11:10:00+09:00',
    '2026-03-04T11:40:00+09:00',
    NULL,
    'tester', 'tester'
FROM tb_brand b
JOIN tb_product p
  ON p.brand_id = b.brand_id
 AND p.item_code = 'ITEM-101'
WHERE b.brand_code = 'BR-002'
    ON CONFLICT (order_no) DO NOTHING;

-- 4) Delivery (정산 기준일 = delivery_dt)
-- 2026-02-27 배송완료 2건
INSERT INTO tb_delivery (order_id, delivery_dt, delivery_fee, created_by, updated_by)
SELECT
    o.order_id,
    '2026-02-27 20:30:00',
    3000.00,
    'tester',
    'tester'
FROM tb_order o
WHERE o.order_no = 'ORD-20260227-0001'
   ON CONFLICT (order_id) DO UPDATE
  SET
     delivery_dt  = EXCLUDED.delivery_dt,
     delivery_fee = EXCLUDED.delivery_fee,
     updated_at   = CURRENT_TIMESTAMP,
     updated_by   = 'tester';

INSERT INTO tb_delivery (order_id, delivery_dt, delivery_fee, created_by, updated_by)
SELECT
    o.order_id,
   '2026-02-27 22:00:00',
   0.00,
   'tester',
   'tester'
FROM tb_order o
WHERE o.order_no = 'ORD-20260227-0002'
   ON CONFLICT (order_id) DO UPDATE
  SET
     delivery_dt = EXCLUDED.delivery_dt,
     delivery_fee = EXCLUDED.delivery_fee,
     updated_at = CURRENT_TIMESTAMP,
     updated_by = 'tester';

-- 2026-03-03 배송완료 3건
INSERT INTO tb_delivery (order_id, delivery_dt, delivery_fee, created_by, updated_by)
SELECT
    o.order_id,
    '2026-03-03 10:30:00',
    2500.00,
    'tester',
    'tester'
FROM tb_order o
WHERE o.order_no = 'ORD-20260303-0001'
   ON CONFLICT (order_id) DO UPDATE
  SET
      delivery_dt = EXCLUDED.delivery_dt,
      delivery_fee = EXCLUDED.delivery_fee,
      updated_at = CURRENT_TIMESTAMP,
      updated_by = 'tester';

INSERT INTO tb_delivery (order_id, delivery_dt, delivery_fee, created_by, updated_by)
SELECT
    o.order_id,
    '2026-03-03 12:30:00',
    3000.00,
    'tester',
    'tester'
FROM tb_order o
WHERE o.order_no = 'ORD-20260303-0002'
   ON CONFLICT (order_id) DO UPDATE
  SET
      delivery_dt = EXCLUDED.delivery_dt,
      delivery_fee = EXCLUDED.delivery_fee,
      updated_at = CURRENT_TIMESTAMP,
      updated_by = 'tester';

INSERT INTO tb_delivery (order_id, delivery_dt, delivery_fee, created_by, updated_by)
SELECT
    o.order_id,
    '2026-03-03 14:30:00',
    0.00,
    'tester',
    'tester'
FROM tb_order o
WHERE o.order_no = 'ORD-20260303-0003'
   ON CONFLICT (order_id) DO UPDATE
  SET
      delivery_dt = EXCLUDED.delivery_dt,
      delivery_fee = EXCLUDED.delivery_fee,
      updated_at = CURRENT_TIMESTAMP,
      updated_by = 'tester';

-- 2026-03-04 배송완료 2건
INSERT INTO tb_delivery (order_id, delivery_dt, delivery_fee, created_by, updated_by)
SELECT
    o.order_id,
    '2026-03-04 10:30:00',
    2500.00,
    'tester',
    'tester'
FROM tb_order o
WHERE o.order_no = 'ORD-20260304-0001'
   ON CONFLICT (order_id) DO UPDATE
  SET
      delivery_dt = EXCLUDED.delivery_dt,
      delivery_fee = EXCLUDED.delivery_fee,
      updated_at = CURRENT_TIMESTAMP,
      updated_by = 'tester';

INSERT INTO tb_delivery (order_id, delivery_dt, delivery_fee, created_by, updated_by)
SELECT
    o.order_id,
    '2026-03-04 12:10:00',
    0.00,
    'tester',
    'tester'
FROM tb_order o
WHERE o.order_no = 'ORD-20260304-0002'
   ON CONFLICT (order_id) DO UPDATE
  SET
      delivery_dt = EXCLUDED.delivery_dt,
      delivery_fee = EXCLUDED.delivery_fee,
      updated_at = CURRENT_TIMESTAMP,
      updated_by = 'tester';