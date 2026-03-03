package com.shop.sales.application

import com.shop.common.error.ApiException
import com.shop.common.error.ErrorCode
import com.shop.sales.api.dto.SalesDetailResponse
import com.shop.sales.domain.repository.SalesDetailRepository
import org.springframework.stereotype.Service

@Service
class SalesQueryService(
    private val salesDetailRepository: SalesDetailRepository
) {
    fun getDetail(orderNo: String): SalesDetailResponse {
        val e = salesDetailRepository.findByOrderNo(orderNo)
            .orElseThrow { ApiException(ErrorCode.NOT_FOUND, "정산 원장이 없습니다: $orderNo") }

        return SalesDetailResponse(
            orderNo = e.orderNo,
            salesDate = e.salesDate,
            brandId = e.brandId,
            itemCode = e.itemCode,
            itemName = e.itemName,
            qty = e.qty,
            amount = e.amount,
            deliveryFee = e.deliveryFee,
            status = e.status
        )
    }
}