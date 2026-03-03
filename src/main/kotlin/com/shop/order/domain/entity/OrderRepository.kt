package com.shop.order.domain.repository

import com.shop.order.domain.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface OrderRepository : JpaRepository<OrderEntity, Long> {
    fun findByOrderNo(orderNo: String): Optional<OrderEntity>
    fun existsByOrderNo(orderNo: String): Boolean
}