package com.shop.sales.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "tb_sales_detail")
class SalesDetailEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sales_detail_id")
    var id: Long? = null,

    @Column(name = "order_no", nullable = false, unique = true)
    var orderNo: String,

    @Column(name = "sales_date", nullable = false)
    var salesDate: LocalDate,

    @Column(name = "brand_id", nullable = false)
    var brandId: Long,

    @Column(name = "item_code", nullable = false)
    var itemCode: String,

    @Column(name = "item_name", nullable = false)
    var itemName: String,

    @Column(name = "qty", nullable = false)
    var qty: Int,

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    var amount: BigDecimal,

    @Column(name = "delivery_fee", nullable = false, precision = 15, scale = 2)
    var deliveryFee: BigDecimal,

    @Column(name = "status", nullable = false)
    var status: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)