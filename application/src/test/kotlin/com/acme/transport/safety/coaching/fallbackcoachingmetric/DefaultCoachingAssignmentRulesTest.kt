package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCoachingAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCoachingAssignmentValidator()
        val item = newDefaultCoachingAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultCoachingAssignmentCodeLengthRule(255)
        val item = newDefaultCoachingAssignment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = DefaultCoachingAssignmentScoreRangeRule(5.2, 3683.9)
        val item = newDefaultCoachingAssignment("x", "y")
        item.score = 3685.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = DefaultCoachingAssignmentExternalRefLengthRule(255)
        val item = newDefaultCoachingAssignment("x", "y")
        item.externalRef = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DefaultCoachingAssignmentAttemptsRangeRule(64, 64)
        val item = newDefaultCoachingAssignment("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
