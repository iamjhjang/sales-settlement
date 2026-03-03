package com.shop.common.error

class ApiException(val errorCode: ErrorCode, override val message: String = errorCode.message)
    : RuntimeException(message)