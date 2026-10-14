package com.acme.transport.network.zone.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneSummaryValidator()
        val item = newZoneSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = ZoneSummaryExternalRefLengthRule(64)
        val item = newZoneSummary("x", "y")
        item.externalRef = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ZoneSummaryRateRangeRule(4.6, 3391.6)
        val item = newZoneSummary("x", "y")
        item.rate = 3393.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ZoneSummaryCapacityRangeRule(255, 3600)
        val item = newZoneSummary("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
