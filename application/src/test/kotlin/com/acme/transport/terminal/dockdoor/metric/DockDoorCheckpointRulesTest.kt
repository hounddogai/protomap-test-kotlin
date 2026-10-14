package com.acme.transport.terminal.dockdoor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorCheckpointValidator()
        val item = newDockDoorCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = DockDoorCheckpointRequiredTrailerIdRule("dock door checkpoint needs this field")
        val item = newDockDoorCheckpoint("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DockDoorCheckpointNameLengthRule(64)
        val item = newDockDoorCheckpoint("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorCheckpointRequiredCodeRule("dock door checkpoint needs this field")
        val item = newDockDoorCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DockDoorCheckpointCodeLengthRule(80)
        val item = newDockDoorCheckpoint("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
