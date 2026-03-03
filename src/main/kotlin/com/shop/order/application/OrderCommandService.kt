package com.shop.order.application

import com.shop.common.error.ApiException
import com.shop.common.error.ErrorCode
import com.shop.catalog.domain.repository.ProductRepository
import com.shop.order.api.dto.ChangeOrderStatusRequest
import com.shop.order.api.dto.CreateOrderRequest
import com.shop.order.domain.entity.DeliveryEntity
import com.shop.order.domain.entity.OrderEntity
import com.shop.order.domain.entity.OrderStatus
import com.shop.order.domain.repository.DeliveryRepository
import com.shop.order.domain.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class OrderCommandService(
    private val orderRepository: OrderRepository,
    private val deliveryRepository: DeliveryRepository,
    private val productRepository: ProductRepository,
) {

    @Transactional
    fun create(req: CreateOrderRequest): OrderEntity {
        if (orderRepository.existsByOrderNo(req.orderNo)) {
            throw ApiException(ErrorCode.DUPLICATE, "이미 존재하는 orderNo 입니다: ${req.orderNo}")
        }

        val product = productRepository.findById(req.productId)
            .orElseThrow { ApiException(ErrorCode.NOT_FOUND, "상품이 존재하지 않습니다: ${req.productId}") }

        // brand 검증(상품의 brand_id와 요청 brandId 일치)
        if (product.brandId != req.brandId) {
            throw ApiException(ErrorCode.BAD_REQUEST, "brandId가 상품과 일치하지 않습니다.")
        }

        val amount = req.unitPrice.multiply(BigDecimal(req.qty))

        return orderRepository.save(
            OrderEntity(
                orderNo = req.orderNo,
                brandId = req.brandId,
                productId = req.productId,
                itemCode = product.itemCode,
                itemName = product.itemName,
                qty = req.qty,
                unitPrice = req.unitPrice,
                amount = amount,
                status = OrderStatus.ORDER,
                orderedAt = OffsetDateTime.now()
            )
        )
    }

    @Transactional
    fun changeStatus(orderNo: String, req: ChangeOrderStatusRequest): OrderEntity {
        val order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow { ApiException(ErrorCode.NOT_FOUND, "주문이 존재하지 않습니다: $orderNo") }

        when (req.status) {
            OrderStatus.COMPLETED -> {
                val deliveryDt = req.deliveryDt ?: throw ApiException(ErrorCode.BAD_REQUEST, "COMPLETED는 deliveryDt가 필수입니다.")
                val deliveryFee = req.deliveryFee ?: BigDecimal.ZERO

                order.status = OrderStatus.COMPLETED
                order.statusChangedAt = OffsetDateTime.now()

                val saved = orderRepository.save(order)

                val existing = deliveryRepository.findByOrderId(saved.id!!)
                val delivery = if (existing.isPresent) {
                    val e = existing.get()
                    e.deliveryDt = deliveryDt
                    e.deliveryFee = deliveryFee
                    e
                } else {
                    DeliveryEntity(orderId = saved.id!!, deliveryDt = deliveryDt, deliveryFee = deliveryFee)
                }
                deliveryRepository.save(delivery)
                return saved
            }

            OrderStatus.CANCELED -> {
                order.status = OrderStatus.CANCELED
                order.statusChangedAt = OffsetDateTime.now()
                return orderRepository.save(order)
            }

            OrderStatus.ORDER -> {
                // 보통 ORDER로 되돌리는 건 막음(필요 시 정책)
                throw ApiException(ErrorCode.INVALID_STATE, "ORDER로 변경은 허용하지 않습니다.")
            }
        }
    }
}