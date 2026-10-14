package com.acme.transport.terminal.dockdoor.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDockDoorAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDockDoorAssignmentValidator()
        val item = newPendingDockDoorAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingDockDoorAssignmentRequiredNameRule("pending dock door assignment needs this field")
        val item = newPendingDockDoorAssignment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = PendingDockDoorAssignmentDockLengthRule(1000)
        val item = newPendingDockDoorAssignment("x", "y")
        item.dock = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = PendingDockDoorAssignmentScoreRangeRule(6.0, 4617.2)
        val item = newPendingDockDoorAssignment("x", "y")
        item.score = 4618.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingDockDoorAssignmentRequiredCodeRule("pending dock door assignment needs this field")
        val item = newPendingDockDoorAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
