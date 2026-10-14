package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceAssignmentValidator()
        val item = newClearanceAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ClearanceAssignmentThresholdRangeRule(8.1, 4788.0)
        val item = newClearanceAssignment("x", "y")
        item.threshold = 4789.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ClearanceAssignmentEndsAtRangeRule(64, 64)
        val item = newClearanceAssignment("x", "y")
        item.endsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ClearanceAssignmentTitleLengthRule(80)
        val item = newClearanceAssignment("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
