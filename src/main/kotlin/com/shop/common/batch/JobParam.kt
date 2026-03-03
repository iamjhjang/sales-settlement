package com.shop.common.batch

import java.time.LocalDate
import java.time.ZoneId

object JobParam {
    const val BUSINESS_DATE = "businessDate" // yyyy-MM-dd
    private val DEFAULT_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")

    fun requireBusinessDate(raw: String?): LocalDate =
        requireNotNull(raw) { "jobParameter '${BUSINESS_DATE}' is required. ex) businessDate=2026-03-02" }
            .let(LocalDate::parse)

    fun businessDateOrToday(raw: String?): LocalDate =
        raw?.let(LocalDate::parse) ?: LocalDate.now(DEFAULT_ZONE_ID)
}