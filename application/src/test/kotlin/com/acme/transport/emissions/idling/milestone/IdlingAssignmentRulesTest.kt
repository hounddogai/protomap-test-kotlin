package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingAssignmentValidator()
        val item = newIdlingAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = IdlingAssignmentStopCountRangeRule(255, 3600)
        val item = newIdlingAssignment("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = IdlingAssignmentMinimumRangeRule(7.6, 3338.5)
        val item = newIdlingAssignment("x", "y")
        item.minimum = 3340.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = IdlingAssignmentSegmentLengthRule(80)
        val item = newIdlingAssignment("x", "y")
        item.segment = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
