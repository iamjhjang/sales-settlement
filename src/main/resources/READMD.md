# 커머스 정산 시스템 : 주문/배송/정산(일·월·년)
Kotlin + Spring Boot 기반으로 주문 → 배송완료/취소 → 정산(일/월/년 집계) 흐름을 구현한다.
월 100만 건 적재를 가정하고, 정산 조회를 브랜드 / 품목코드 / 주문번호 기준으로 빠르게 제공하는 것을 목표로 한다.
 -> 정산 기준일은 (배송완료일시) 기준

## 1. 목표
- Kotlin + Spring Boot로 주문/배송완료/취소/정산 흐름 구현
- 정산 집계는 Spring Batch로 수행
- PostgreSQL + Flyway로 스키마 관리
- Swagger(OpenAPI)로 API 테스트 가능
- 조회 성능: 브랜드/품목코드/주문번호 기준 빠른 조회
- 
### 1.1 주요 도메인
* Catalog: 브랜드/상품/전시(선택)
* Order: 주문, 주문상태(배송완료/취소)
* Sales: 정산 원장 및 집계(일/월/년)
* Common: 공통 응답/예외/유틸/로깅/상수


## 2. 기술스팩
- Language: Kotlin
- Framework: Spring Boot 4.x, Spring Web, Validation, Spring Data JPA
- Batch: Spring Batch
- DB: PostgreSQL
- Migration: Flyway
- API Doc: springdoc-openapi (Swagger UI)
- Build: Gradle (Kotlin DSL)
- Local Runtime: Docker Compose


## 3. 비지니스 규칙
### 3.1 주문 상태
- `ORDER` : 주문요청
- `COMPLETED` : 배송완료된 주문(정산 대상)
- `CANCELED` : 취소 주문(정산 제외)

### 3.2 정산 기준일(중요)
- 정산 기준일 = `tb_delivery.delivery_dt` (배송완료일시)
- 원장(`tb_sales_detail`)의 `sales_date`는 `date(delivery_dt)`로 저장
- 일별 집계(`tb_sales_daily.sales_day`)도 동일하게 `date(delivery_dt)` 기준
- 취소(`CANCELED`) 주문은 정산에서 제외

### 3.3 주문/정산 데이터(주문/원장/집계)
- 브랜드 : `tb_brand`
- 상품 : `tb_product`
- 주문 : `tb_order`, `tb_delivery`
- 정산 원장: `tb_sales_detail`
- 집계 테이블:
  - 일별: `tb_sales_daily` (sales_day)
  - 월별: `tb_sales_monthly` (sales_month = YYYY-MM)
  - 연별: `tb_sales_yearly` (sales_year = YYYY)

### 3.4 중복 집계 방지
- 정산 배치는 `tb_order.settled_at IS NULL AND tb_order.status = 'COMPLETED'` 인 주문만 처리
- 처리 성공 시 `tb_order.settled_at = now()` 업데이트
- 원장/집계 테이블은 UNIQUE KEY 기반 UPSERT로 재실행 시 중복 방지


## 4. 처리 흐름(Flow)
  A [주문 생성 API]
     -> (주문요청) [tb_order 저장]
  B [주문 상태 변경 API]
     -> (배송완료) [tb_order.status=COMPLETED / tb_order.status_changed_at=getdate(), tb_delivery 저장 (delivery_dt)]
     -> (주문취소) [tb_order.status=CANCELED  / tb_order.status_changed_at=getdate()]
  C [Spring Batch 정산 집계 Job]
     -> (원장집계) [tb_sales_detail UPSERT]
     -> (일/월/일) [tb_sales_daily/monthly/yearly UPSERT]
     -> (집계완료) [tb_order.settled_at UPSERT ]
  D [정산 조회 API]


## 5. 스키마
### 5.1 Catalog
* tb_brand
  - brand_id(PK), brand_code(UNIQUE), name, status, 
    created_at, updated_at , created_by, updated_by
* tb_product
  - product_id(PK), brand_id, item_code(UNIQUE), item_name, status, 
    created_at, updated_at , created_by, updated_by
  
### 5.2 Order/Delivery
* tb_order
  - orders
    order_id(PK), order_no(UNIQUE) ,brand_id, product_id, item_code, item_name ,qty, unit_price, amount , status, ordered_at , status_changed_at, settled_at,
    created_at, updated_at , created_by, updated_by
  - indexes: 
      1) status , settled_at (집계용)
      2) brand_id, status, ordered_at ( 조회용)
* tb_delivery
  - delivery_id(PK), order_id(UNIQUE) , delivery_dt, delivery_fee , 
    created_by , updated_at, created_by  , updated_by
  - indexes: 
      1) delivery_dt(조회용)
    
### 5.3 Sales
* tb_sales_detail
  - sales_detail_id(PK),order_no(UNIQUE) , sales_date, brand_id, item_code,item_name, qty, amount, delivery_fee, status,
    created_at, updated_at , created_by, updated_by
  - indexes: 
      1) sales_date , brand_id , item_code(조회용)
* tb_sales_daily
  - sales_daily_id(PK), sales_day , brand_id , item_code,item_name, order_cnt , qty_sum, amount_sum ,delivery_fee_sum,
    created_at, updated_at , created_by, updated_by
  - UNIQUE: 
      1) sales_day , brand_id , item_code 
* tb_sales_monthly
  - sales_monthly_id(PK), sales_month , brand_id , item_code,item_name, order_cnt,qty_sum, amount_sum ,delivery_fee_sum,
    created_at, updated_at , created_by, updated_by
  - UNIQUE: 
      1) sales_month , brand_id , item_code
* tb_sales_yearly
  - sales_yearly_id(PK), sales_year , brand_id , item_code, item_name , order_cnt, qty_sum, amount_sum , delivery_fee_sum,
    created_at, updated_at , created_by, updated_by
  - UNIQUE: 
      1) sales_year , brand_id , item_code
    2) 

## 6. 배치
### 6.1 Job: salesSettlementJob
#### 6.1.1 배치대상
  - `tb_order.status = 'COMPLETED' ` (배송완료)
  - `tb_order.settled_at IS NULL ` (정산 미처리)
  - `tb_delivery.delivery_dt` (정산 기준일)

#### 6.1.2 배치대상
  - `tb_sales_detail ` (원장 UPSERT)
  - `tb_sales_daily/monthly/yearly` (일/월/년 UPSERT)
  - `tb_order.settled_at = now()` (정산완료)


## 7. API 
Swagger UI : http://localhost:8080/swagger-ui.html

### 7.1 Order
 - 주문 생성    : POST /api/v1/orders
 - 주문 상태변경 : POST /api/v1/orders/status

### 7.2 Sales
 - 원장 조회 : POST /api/v1/sales/detail
 - 일원장보회: POST /api/v1/sales/daily
 - 월원장보회: POST /api/v1/sales/monthly
 - 년원장보회: POST /api/v1/sales/yearly