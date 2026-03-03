package com.shop.sales.api.dto

import java.math.BigDecimal
import java.time.LocalDate

data class SalesDetailResponse(
    val orderNo: String,
    val salesDate: LocalDate,
    val brandId: Long,
    val itemCode: String,
    val itemName: String,
    val qty: Int,
    val amount: BigDecimal,
    val deliveryFee: BigDecimal,
    val status: String
)