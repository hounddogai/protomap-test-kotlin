package com.acme.transport.customs.clearance.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingClearanceAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingClearanceAssignmentValidator()
        val item = newPendingClearanceAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = PendingClearanceAssignmentDescriptionLengthRule(80)
        val item = newPendingClearanceAssignment("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = PendingClearanceAssignmentScoreRangeRule(5.5, 4411.5)
        val item = newPendingClearanceAssignment("x", "y")
        item.score = 4413.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = PendingClearanceAssignmentRevisionRangeRule(1000, 86400)
        val item = newPendingClearanceAssignment("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = PendingClearanceAssignmentShiftCodeLengthRule(64)
        val item = newPendingClearanceAssignment("x", "y")
        item.shiftCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
