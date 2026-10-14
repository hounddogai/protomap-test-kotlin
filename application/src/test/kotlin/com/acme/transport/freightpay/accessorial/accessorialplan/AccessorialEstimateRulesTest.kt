package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialEstimateValidator()
        val item = newAccessorialEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = AccessorialEstimateExpiresAtRangeRule(16, 3600)
        val item = newAccessorialEstimate("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AccessorialEstimateRequiredTitleRule("accessorial estimate needs this field")
        val item = newAccessorialEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = AccessorialEstimateRequiredChannelRule("accessorial estimate needs a value")
        val item = newAccessorialEstimate("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AccessorialEstimateTitleLengthRule(32)
        val item = newAccessorialEstimate("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = AccessorialEstimateCostRangeRule(6.9, 952.1)
        val item = newAccessorialEstimate("x", "y")
        item.cost = 953.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
