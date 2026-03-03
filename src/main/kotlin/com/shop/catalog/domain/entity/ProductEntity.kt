package com.shop.catalog.domain.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "tb_product")
class ProductEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    var id: Long? = null,

    @Column(name = "brand_id", nullable = false)
    var brandId: Long,

    @Column(name = "item_code", nullable = false, unique = true, length = 50)
    var itemCode: String,

    @Column(name = "item_name", nullable = false, length = 200)
    var itemName: String,

    @Column(name = "status", nullable = false, length = 20)
    var status: String = "ACTIVE",

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "created_by", nullable = false)
    var createdBy: String = "system",

    @Column(name = "updated_by", nullable = false)
    var updatedBy: String = "system",
)