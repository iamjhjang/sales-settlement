package com.shop.common.batch

import java.time.LocalDate

object JobParam {
    const val BUSINESS_DATE = "businessDate" // yyyy-MM-dd

    fun requireBusinessDate(raw: String?): LocalDate =
        requireNotNull(raw) { "jobParameter '${BUSINESS_DATE}' is required. ex) businessDate=2026-03-02" }
            .let(LocalDate::parse)
}