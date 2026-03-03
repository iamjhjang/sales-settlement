package com.shop.order.domain.repository

import com.shop.order.domain.entity.DeliveryEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface DeliveryRepository : JpaRepository<DeliveryEntity, Long> {
    fun findByOrderId(orderId: Long): Optional<DeliveryEntity>
}