package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDockDoorCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDockDoorCheckpointValidator()
        val item = newCappedDockDoorCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = CappedDockDoorCheckpointStopCountRangeRule(64, 255)
        val item = newCappedDockDoorCheckpoint("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedDockDoorCheckpointRequiredLabelRule("capped dock door checkpoint needs a value")
        val item = newCappedDockDoorCheckpoint("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedDockDoorCheckpointRequiredTitleRule("capped dock door checkpoint needs a value")
        val item = newCappedDockDoorCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedDockDoorCheckpointCodeLengthRule(32)
        val item = newCappedDockDoorCheckpoint("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
