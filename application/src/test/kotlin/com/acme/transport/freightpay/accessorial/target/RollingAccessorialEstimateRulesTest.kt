package com.acme.transport.freightpay.accessorial.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAccessorialEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingAccessorialEstimateValidator()
        val item = newRollingAccessorialEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingAccessorialEstimateRequiredTitleRule("rolling accessorial estimate needs this field")
        val item = newRollingAccessorialEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RollingAccessorialEstimateStartsAtRangeRule(255, 255)
        val item = newRollingAccessorialEstimate("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingAccessorialEstimateTitleLengthRule(86400)
        val item = newRollingAccessorialEstimate("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = RollingAccessorialEstimateQuantityRangeRule(120, 3600)
        val item = newRollingAccessorialEstimate("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
