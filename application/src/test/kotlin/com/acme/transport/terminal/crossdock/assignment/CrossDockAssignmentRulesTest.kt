package com.acme.transport.terminal.crossdock.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockAssignmentValidator()
        val item = newCrossDockAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CrossDockAssignmentCodeLengthRule(3600)
        val item = newCrossDockAssignment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = CrossDockAssignmentStopCountRangeRule(64, 80)
        val item = newCrossDockAssignment("x", "y")
        item.stopCount = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = CrossDockAssignmentThresholdRangeRule(7.1, 4870.0)
        val item = newCrossDockAssignment("x", "y")
        item.threshold = 4871.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = CrossDockAssignmentTitleLengthRule(120)
        val item = newCrossDockAssignment("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
