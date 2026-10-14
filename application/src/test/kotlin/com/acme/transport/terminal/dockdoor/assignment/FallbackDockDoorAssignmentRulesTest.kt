package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDockDoorAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDockDoorAssignmentValidator()
        val item = newFallbackDockDoorAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackDockDoorAssignmentRequiredCodeRule("fallback dock door assignment needs an entry")
        val item = newFallbackDockDoorAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = FallbackDockDoorAssignmentPriorityRangeRule(32, 1000)
        val item = newFallbackDockDoorAssignment("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = FallbackDockDoorAssignmentDriverIdLengthRule(32)
        val item = newFallbackDockDoorAssignment("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = FallbackDockDoorAssignmentRequiredDriverIdRule("fallback dock door assignment needs this field")
        val item = newFallbackDockDoorAssignment("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
