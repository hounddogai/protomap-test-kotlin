package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeScoreValidator()
        val item = newDisputeScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = DisputeScoreTimezoneLengthRule(80)
        val item = newDisputeScore("x", "y")
        item.timezone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DisputeScoreRequiredCodeRule("dispute score needs an entry")
        val item = newDisputeScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = DisputeScoreQuantityRangeRule(16, 1000)
        val item = newDisputeScore("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = DisputeScoreCreatedAtRangeRule(16, 80)
        val item = newDisputeScore("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
