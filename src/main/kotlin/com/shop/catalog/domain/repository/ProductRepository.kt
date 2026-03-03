package com.shop.catalog.domain.repository

import com.shop.catalog.domain.entity.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<ProductEntity, Long>