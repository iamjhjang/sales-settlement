package com.shop.common.batch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class JobParamTest {

    @Test
    fun `requireBusinessDate throws when value is missing`() {
        assertThrows(IllegalArgumentException::class.java) {
            JobParam.requireBusinessDate(null)
        }
    }

    @Test
    fun `businessDateOrToday returns parsed value when value is provided`() {
        val date = JobParam.businessDateOrToday("2026-03-02")

        assertEquals(LocalDate.of(2026, 3, 2), date)
    }

    @Test
    fun `businessDateOrToday defaults to today in seoul when value is missing`() {
        val date = JobParam.businessDateOrToday(null)

        assertEquals(LocalDate.now(ZoneId.of("Asia/Seoul")), date)
    }
}
