package com.shop.sales.domain.repository

import com.shop.sales.domain.entity.SalesDetailEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface SalesDetailRepository : JpaRepository<SalesDetailEntity, Long> {
    fun findByOrderNo(orderNo: String): Optional<SalesDetailEntity>
}