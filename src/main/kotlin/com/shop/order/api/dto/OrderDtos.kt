package com.shop.order.api.dto

import com.shop.order.domain.entity.OrderStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.OffsetDateTime

data class CreateOrderRequest(
    @field:NotBlank(message = "orderNo는 필수입니다.")
    val orderNo: String,

    @field:NotNull(message = "brandId는 필수입니다.")
    val brandId: Long,

    @field:NotNull(message = "productId는 필수입니다.")
    val productId: Long,

    @field:Positive(message = "qty는 1 이상이어야 합니다.")
    val qty: Int,

    @field:NotNull(message = "unitPrice는 필수입니다.")
    val unitPrice: BigDecimal,
)

data class CreateOrderResponse(
    val orderNo: String,
    val status: OrderStatus,
    val amount: BigDecimal,
)

data class ChangeOrderStatusRequest(
    @field:NotNull(message = "status는 필수입니다.")
    val status: OrderStatus,

    // 배송완료(COMPLETED)일 때만 필수
    val deliveryDt: OffsetDateTime? = null,
    val deliveryFee: BigDecimal? = null
)

data class ChangeOrderStatusResponse(
    val orderNo: String,
    val status: OrderStatus
)