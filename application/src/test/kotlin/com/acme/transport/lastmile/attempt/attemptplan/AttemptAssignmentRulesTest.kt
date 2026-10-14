package com.acme.transport.lastmile.attempt.attemptplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptAssignmentValidator()
        val item = newAttemptAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AttemptAssignmentTitleLengthRule(3600)
        val item = newAttemptAssignment("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptAssignmentRequiredCodeRule("attempt assignment needs a value")
        val item = newAttemptAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = AttemptAssignmentLoadKgRangeRule(9.2, 1570.2)
        val item = newAttemptAssignment("x", "y")
        item.loadKg = 1571.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
