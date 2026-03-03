package com.shop.common.error

enum class ErrorCode(val code: String, val message: String) {
    NOT_FOUND("NOT_FOUND", "데이터를 찾을 수 없습니다."),
    INVALID_STATE("INVALID_STATE", "상태값이 올바르지 않습니다."),
    DUPLICATE("DUPLICATE", "중복 데이터입니다."),
    BAD_REQUEST("BAD_REQUEST", "요청이 올바르지 않습니다.")
}