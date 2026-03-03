# 목표와 범위

### 목표
* Kotlin + Spring Boot 기반으로 주문 → 배송완료/취소 → 정산(일/월/년 집계) 흐름을 구현한다.
* 월 100만 건 적재를 가정하고, 정산 조회를 브랜드/품목코드/주문번호 기준으로 빠르게 제공한다.
* Spring Batch로 정산 집계를 수행하고, PostgreSQL + Flyway로 스키마를 관리한다.
* Swagger(OpenAPI)로 API 테스트가 가능하도록 한다.

### 주요 도메인
* Catalog: 브랜드/상품/전시(선택)
* Order: 주문, 주문상태(배송완료/취소)
* Sales: 정산 원장 및 집계(일/월/년)
* Common: 공통 응답/예외/유틸/로깅/상수

# 기술 스택
* Language: Kotlin
* Framework: Spring Boot 4.x, Spring Web, Validation, Spring Data JPA
* Batch: Spring Batch
* DB: PostgreSQL
* Migration: Flyway
* API Doc: springdoc-openapi (Swagger UI)
* Build: Gradle (Kotlin DSL 권장)
* Local runtime: Docker Compose

# 아치텍쳐

### Catalog
* tb_brand
  - brand_id(PK), brand_code, name, status, created_at, updated_at , created_by, updated_by
  - indexes: brand_code(UNIQUE)
* tb_product
  - product_id(PK), brand_id(FK), item_code(품목코드), item_name, status, created_at, updated_at , created_by, updated_by 
  - indexes: item_code(UNIQUE)
* tb_order
  - orders
    order_id(PK), order_no ,brand_id, product_id, item_code, item_name ,qty, unit_price, amount , status(COMPLETED|CANCELED),
    ordered_at , status_changed_at, settled_at,
    created_at, updated_at , created_by, updated_by
  - indexes: order_no(UNIQUE)
           status , settled_at(INDEX)
           brand_id, status, ordered_at(INDEX)
* tb_delivery
  - delivery_id(PK), order_id , delivery_dt, delivery_fee , created_by , created_at, updated_by  , updated_by
  - indexes: order_id(UNIQUE) , 
                   delivery_dt(INDEX)
* tb_sales_detail
  - sales_detail_id(PK),order_no , sales_date, brand_id, item_code,item_name, qty, amount, delivery_fee, status,
    created_at, updated_at , created_by, updated_by
  - indexes: order_no (UNIQUE) ,  
             sales_date , brand_id , item_code(INDEX)
* tb_sales_daily
  - sales_daily_id(PK), sales_day , brand_id , item_code,item_name, order_cnt , qty_sum, amount_sum ,delivery_fee_sum,
    created_at, updated_at , created_by, updated_by
  - indexes: sales_day , brand_id , item_code (UNIQUE) 
* tb_sales_monthly
  - sales_monthly_id(PK), sales_month , brand_id , item_code,item_name, order_cnt,qty_sum, amount_sum ,delivery_fee_sum,
    created_at, updated_at , created_by, updated_by
  - indexes: sales_month , brand_id , item_code(UNIQUE)
* tb_sales_yearly
  - sales_yearly_id(PK), sales_year , brand_id , item_code, item_name , order_cnt, qty_sum, amount_sum , delivery_fee_sum,
    created_at, updated_at , created_by, updated_by
  - indexes: sales_year , brand_id , item_code(UNIQUE)