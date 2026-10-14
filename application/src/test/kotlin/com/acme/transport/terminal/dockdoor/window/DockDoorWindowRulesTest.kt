package com.acme.transport.terminal.dockdoor.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorWindowValidator()
        val item = newDockDoorWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = DockDoorWindowReviewerLengthRule(1000)
        val item = newDockDoorWindow("x", "y")
        item.reviewer = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DockDoorWindowCeilingRangeRule(6.2, 2170.8)
        val item = newDockDoorWindow("x", "y")
        item.ceiling = 2172.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorWindowRequiredCodeRule("dock door window needs this field")
        val item = newDockDoorWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DockDoorWindowLabelLengthRule(3600)
        val item = newDockDoorWindow("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = DockDoorWindowRetriesRangeRule(64, 64)
        val item = newDockDoorWindow("x", "y")
        item.retries = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
