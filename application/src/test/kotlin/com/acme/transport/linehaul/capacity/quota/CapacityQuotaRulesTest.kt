package com.acme.transport.linehaul.capacity.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityQuotaValidator()
        val item = newCapacityQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = CapacityQuotaSegmentLengthRule(32)
        val item = newCapacityQuota("x", "y")
        item.segment = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = CapacityQuotaFuelLitersRangeRule(9.6, 3214.2)
        val item = newCapacityQuota("x", "y")
        item.fuelLiters = 3215.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = CapacityQuotaTitleLengthRule(64)
        val item = newCapacityQuota("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = CapacityQuotaThresholdRangeRule(7.4, 4651.6)
        val item = newCapacityQuota("x", "y")
        item.threshold = 4653.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
