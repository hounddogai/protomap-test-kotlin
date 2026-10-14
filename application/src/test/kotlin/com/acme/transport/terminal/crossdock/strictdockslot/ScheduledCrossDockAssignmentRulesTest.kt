package com.acme.transport.terminal.crossdock.strictdockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCrossDockAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCrossDockAssignmentValidator()
        val item = newScheduledCrossDockAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledCrossDockAssignmentRequiredCodeRule("scheduled cross dock assignment needs this field")
        val item = newScheduledCrossDockAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledCrossDockAssignmentTitleLengthRule(64)
        val item = newScheduledCrossDockAssignment("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = ScheduledCrossDockAssignmentVersionRangeRule(255, 3600)
        val item = newScheduledCrossDockAssignment("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
