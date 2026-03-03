package com.shop.order.api

import com.shop.common.api.ApiResponse
import com.shop.order.api.dto.ChangeOrderStatusRequest
import com.shop.order.api.dto.ChangeOrderStatusResponse
import com.shop.order.api.dto.CreateOrderRequest
import com.shop.order.api.dto.CreateOrderResponse
import com.shop.order.application.OrderCommandService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderCommandService: OrderCommandService
) {

    @PostMapping
    fun create(@Valid @RequestBody req: CreateOrderRequest): ApiResponse<CreateOrderResponse> {
        val saved = orderCommandService.create(req)
        return ApiResponse.ok(CreateOrderResponse(saved.orderNo, saved.status, saved.amount))
    }

    @PatchMapping("/{orderNo}/status")
    fun changeStatus(
        @PathVariable orderNo: String,
        @Valid @RequestBody req: ChangeOrderStatusRequest
    ): ApiResponse<ChangeOrderStatusResponse> {
        val saved = orderCommandService.changeStatus(orderNo, req)
        return ApiResponse.ok(ChangeOrderStatusResponse(saved.orderNo, saved.status))
    }
}