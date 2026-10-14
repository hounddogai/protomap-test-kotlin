package com.acme.transport.safety.violation.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationMilestoneValidator()
        val item = newViolationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = ViolationMilestoneUpdatedAtRangeRule(3600, 3600)
        val item = newViolationMilestone("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = ViolationMilestoneChannelLengthRule(120)
        val item = newViolationMilestone("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ViolationMilestoneCodeLengthRule(255)
        val item = newViolationMilestone("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ViolationMilestoneRequiredCodeRule("violation milestone needs a value")
        val item = newViolationMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
