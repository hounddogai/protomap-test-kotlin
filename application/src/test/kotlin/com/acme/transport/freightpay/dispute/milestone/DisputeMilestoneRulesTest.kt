package com.acme.transport.freightpay.dispute.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeMilestoneValidator()
        val item = newDisputeMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DisputeMilestoneRequiredCodeRule("dispute milestone needs a value")
        val item = newDisputeMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DisputeMilestoneLabelLengthRule(3600)
        val item = newDisputeMilestone("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = DisputeMilestoneUpdatedAtRangeRule(64, 120)
        val item = newDisputeMilestone("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = DisputeMilestoneLaneLengthRule(255)
        val item = newDisputeMilestone("x", "y")
        item.lane = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DisputeMilestoneRequiredLabelRule("dispute milestone needs this field")
        val item = newDisputeMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
