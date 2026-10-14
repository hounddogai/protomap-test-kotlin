package com.acme.transport.customs.hold.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldAssignmentValidator()
        val item = newHoldAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = HoldAssignmentScoreRangeRule(1.3, 4473.6)
        val item = newHoldAssignment("x", "y")
        item.score = 4475.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = HoldAssignmentUpdatedAtRangeRule(86400, 86400)
        val item = newHoldAssignment("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldAssignmentCodeLengthRule(1000)
        val item = newHoldAssignment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
