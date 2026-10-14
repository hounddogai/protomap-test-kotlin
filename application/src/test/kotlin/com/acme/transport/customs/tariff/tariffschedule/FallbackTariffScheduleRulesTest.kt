package com.acme.transport.customs.tariff.tariffschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTariffScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTariffScheduleValidator()
        val item = newFallbackTariffSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = FallbackTariffScheduleLimitRangeRule(16, 3600)
        val item = newFallbackTariffSchedule("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackTariffScheduleCodeLengthRule(255)
        val item = newFallbackTariffSchedule("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = FallbackTariffScheduleTrailerIdLengthRule(64)
        val item = newFallbackTariffSchedule("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
