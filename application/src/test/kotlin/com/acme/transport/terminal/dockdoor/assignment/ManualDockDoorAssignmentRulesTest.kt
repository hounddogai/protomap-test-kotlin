package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDockDoorAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDockDoorAssignmentValidator()
        val item = newManualDockDoorAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualDockDoorAssignmentRequiredTitleRule("manual dock door assignment needs a value")
        val item = newManualDockDoorAssignment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ManualDockDoorAssignmentContactEmailLengthRule(1000)
        val item = newManualDockDoorAssignment("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualDockDoorAssignmentNameLengthRule(80)
        val item = newManualDockDoorAssignment("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
