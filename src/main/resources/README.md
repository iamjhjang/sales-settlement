# 커머스 정산 시스템 : 주문/배송/정산(일·월·년)
Kotlin + Spring Boot 기반으로 주문 → 배송완료/취소 → 정산(일/월/년 집계) 흐름을 구현한다.
월 100만 건 적재를 가정하고, 정산 조회를 브랜드 / 품목코드 / 주문번호 기준으로 빠르게 제공하는 것을 목표로 한다.
 -> 정산 기준일은 (배송완료일시) 기준

## 1. 목표
- Kotlin + Spring Boot로 주문/배송완료/취소/정산 흐름 구현
- 정산 집계는 Spring Batch로 수행
- PostgreSQL
- Swagger(OpenAPI)로 API 테스트 가능
- 조회 성능: 브랜드/품목코드/주문번호 기준 빠른 조회

### 1.1 주요 도메인
- Catalog: 브랜드/상품/전시(선택)
- Order: 주문, 주문상태(배송완료/취소)
- Sales: 정산 원장 및 집계(일/월/년)
- Common: 공통 응답/예외/유틸/로깅/상수


## 2. 기술스팩
- Language: Kotlin
- Framework: Spring Boot 4.x, Spring Web, Validation, Spring Data JPA
- Batch: Spring Batch
- DB: PostgreSQL
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
     -> (주문요청) [tb_order 저장] (status=ORDER)
  B [주문 상태 변경 API]
     -> (배송완료) [tb_order.status=COMPLETED / tb_order.status_changed_at=CURRENT_TIMESTAMP, tb_delivery 저장 (delivery_dt)]
     -> (주문취소) [tb_order.status=CANCELED  / tb_order.status_changed_at=CURRENT_TIMESTAMP]
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
    created_at , updated_at, created_by  , updated_by
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

## 6. 실행

## 6.1 Quick Start
> DB(PostgreSQL) + 애플리케이션을 한 번에 기동합니다.
> 프로젝트 루트에서 실행
- .\src\main\resources\script\run.ps1

## 6.2 스키마/샘플 데이터 스크립트 
- `src/main/resources/script/schema.sql` : 업무 테이블(tb_*) + Spring Batch 메타 테이블(BATCH_*)
- `src/main/resources/script/data.sql` : 샘플 데이터(브랜드/상품/주문/배송)

## 7. 배치
### 7.1 Job: salesSettlementJob
- 정산 기준일 파라미터: businessDate=yyyy-MM-dd

### 7.2 배치 강제 실행(REST)
> 애플리케이션 기동 후 아래 호출로 배치를 실행합니다.
- curl -X POST "http://localhost:8080/batch/sales-settlement/run?businessDate=2026-02-27"
- curl -X POST "http://localhost:8080/batch/sales-settlement/run?businessDate=2026-03-03"
- curl -X POST "http://localhost:8080/batch/sales-settlement/run?businessDate=2026-03-04"

### 7.3 배치처리대상
  - `tb_order.status = 'COMPLETED' ` (배송완료)
  - `tb_order.settled_at IS NULL ` (정산 미처리)
  - `tb_delivery.delivery_dt` (정산 기준일)

### 7.4 배치처리내용
  - `tb_sales_detail ` (원장 UPSERT)
  - `tb_sales_daily/monthly/yearly` (일/월/년 UPSERT)
  - `tb_order.settled_at = now()` (정산완료)

### 7.5 결과 확인 SQL
> 2026-02-27 집계 확인
- select count(*) from tb_sales_detail where sales_date = '2026-02-27';
- select * from tb_sales_daily   where sales_day   = '2026-02-27' limit 20;
- select * from tb_sales_monthly where sales_month = '2026-02'    limit 20;
- select * from tb_sales_yearly  where sales_year  = '2026'       limit 20;

> 2026-03-03 집계 확인
- select count(*) from tb_sales_detail where sales_date = '2026-03-03';
- select * from tb_sales_daily   where sales_day   = '2026-03-03' limit 20;
- select * from tb_sales_monthly where sales_month = '2026-03'    limit 20;
- select * from tb_sales_yearly  where sales_year  = '2026'       limit 20;

> 정산완료 마킹 확인
select order_no, status, settled_at
from tb_order
where status = 'COMPLETED'
order by order_id desc
limit 20;

## 8. API 
Swagger UI : http://localhost:8080/swagger-ui/index.html

### 8.1 Order
 - 주문 생성    : POST /api/v1/orders
 - 주문 상태변경 : POST /api/v1/orders/status

### 8.2 Sales
- 정산 원장(상세) 조회 : GET /api/v1/sales/detail
- 참고: 일/월/년 집계 데이터는 배치가 `tb_sales_daily/monthly/yearly`에 적재하며,
    README의 "결과 확인 SQL"로 검증합니다.