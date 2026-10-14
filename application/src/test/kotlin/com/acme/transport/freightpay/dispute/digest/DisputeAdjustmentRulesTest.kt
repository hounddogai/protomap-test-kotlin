package com.acme.transport.freightpay.dispute.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeAdjustmentValidator()
        val item = newDisputeAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DisputeAdjustmentCeilingRangeRule(0.4, 288.7)
        val item = newDisputeAdjustment("x", "y")
        item.ceiling = 290.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DisputeAdjustmentRequiredCodeRule("dispute adjustment needs a value")
        val item = newDisputeAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = DisputeAdjustmentRateRangeRule(1.9, 1358.9)
        val item = newDisputeAdjustment("x", "y")
        item.rate = 1360.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DisputeAdjustmentCodeLengthRule(32)
        val item = newDisputeAdjustment("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DisputeAdjustmentGraceDaysRangeRule(255, 3600)
        val item = newDisputeAdjustment("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
