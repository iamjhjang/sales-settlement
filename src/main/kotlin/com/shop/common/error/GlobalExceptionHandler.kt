package com.shop.common.error

import com.shop.common.api.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException) =
        ApiResponse.fail(e.errorCode.code, e.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ApiResponse<Nothing> {
        val msg = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Validation error"
        return ApiResponse.fail(ErrorCode.BAD_REQUEST.code, msg)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnknown(e: Exception): ApiResponse<Nothing> =
        ApiResponse.fail("INTERNAL_ERROR", e.message ?: "unknown error")
}