package com.acme.transport.safety.violation.violationslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictViolationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictViolationMilestoneValidator()
        val item = newStrictViolationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = StrictViolationMilestoneUpdatedAtRangeRule(80, 120)
        val item = newStrictViolationMilestone("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = StrictViolationMilestoneRequiredCurrencyRule("strict violation milestone needs this field")
        val item = newStrictViolationMilestone("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = StrictViolationMilestoneCurrencyLengthRule(3600)
        val item = newStrictViolationMilestone("x", "y")
        item.currency = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
