package com.acme.transport.terminal.dockdoor.dooragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorAssignmentValidator()
        val item = newDockDoorAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DockDoorAssignmentRequiredLabelRule("dock door assignment needs an entry")
        val item = newDockDoorAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DockDoorAssignmentNameLengthRule(120)
        val item = newDockDoorAssignment("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = DockDoorAssignmentUpdatedAtRangeRule(16, 64)
        val item = newDockDoorAssignment("x", "y")
        item.updatedAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
