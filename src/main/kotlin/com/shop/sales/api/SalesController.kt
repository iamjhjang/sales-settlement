package com.shop.sales.api

import com.shop.common.api.ApiResponse
import com.shop.sales.api.dto.SalesDetailResponse
import com.shop.sales.application.SalesQueryService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sales")
class SalesController(
    private val salesQueryService: SalesQueryService
) {
    @GetMapping("/detail")
    fun detail(@RequestParam orderNo: String): ApiResponse<SalesDetailResponse> {
        return ApiResponse.ok(salesQueryService.getDetail(orderNo))
    }
}