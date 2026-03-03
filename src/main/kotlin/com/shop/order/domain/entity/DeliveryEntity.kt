package com.shop.order.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "tb_delivery")
class DeliveryEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    var id: Long? = null,

    @Column(name = "order_id", nullable = false, unique = true)
    var orderId: Long,

    @Column(name = "delivery_dt", nullable = false)
    var deliveryDt: OffsetDateTime,

    @Column(name = "delivery_fee", nullable = false, precision = 15, scale = 2)
    var deliveryFee: BigDecimal = BigDecimal.ZERO,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "created_by", nullable = false)
    var createdBy: String = "system",

    @Column(name = "updated_by", nullable = false)
    var updatedBy: String = "system",
)