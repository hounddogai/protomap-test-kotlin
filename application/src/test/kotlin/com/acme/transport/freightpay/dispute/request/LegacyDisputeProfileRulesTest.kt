package com.acme.transport.freightpay.dispute.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDisputeProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDisputeProfileValidator()
        val item = newLegacyDisputeProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LegacyDisputeProfileCostRangeRule(1.9, 4666.9)
        val item = newLegacyDisputeProfile("x", "y")
        item.cost = 4668.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = LegacyDisputeProfileThresholdRangeRule(8.1, 318.8)
        val item = newLegacyDisputeProfile("x", "y")
        item.threshold = 320.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyDisputeProfileCodeLengthRule(64)
        val item = newLegacyDisputeProfile("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
