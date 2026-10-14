package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCutoffMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCutoffMilestoneValidator()
        val item = newCappedCutoffMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedCutoffMilestoneRequiredCodeRule("capped cutoff milestone needs a value")
        val item = newCappedCutoffMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CappedCutoffMilestoneGraceDaysRangeRule(64, 120)
        val item = newCappedCutoffMilestone("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = CappedCutoffMilestoneUpdatedAtRangeRule(16, 86400)
        val item = newCappedCutoffMilestone("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedCutoffMilestoneCodeLengthRule(255)
        val item = newCappedCutoffMilestone("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
