package com.shop.order.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "tb_order")
class OrderEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    var id: Long? = null,

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    var orderNo: String,

    @Column(name = "brand_id", nullable = false)
    var brandId: Long,

    @Column(name = "product_id", nullable = false)
    var productId: Long,

    @Column(name = "item_code", nullable = false, length = 50)
    var itemCode: String,

    @Column(name = "item_name", nullable = false, length = 200)
    var itemName: String,

    @Column(name = "qty", nullable = false)
    var qty: Int,

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    var unitPrice: BigDecimal,

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.ORDER,

    @Column(name = "ordered_at", nullable = false)
    var orderedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "status_changed_at")
    var statusChangedAt: OffsetDateTime? = null,

    @Column(name = "settled_at")
    var settledAt: OffsetDateTime? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "created_by", nullable = false)
    var createdBy: String = "system",

    @Column(name = "updated_by", nullable = false)
    var updatedBy: String = "system",
)