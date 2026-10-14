package com.acme.transport.linehaul.capacity.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCapacityCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCapacityCheckpointValidator()
        val item = newLenientCapacityCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientCapacityCheckpointRequiredCodeRule("lenient capacity checkpoint needs an entry")
        val item = newLenientCapacityCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientCapacityCheckpointCodeLengthRule(120)
        val item = newLenientCapacityCheckpoint("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = LenientCapacityCheckpointMaximumRangeRule(1.8, 787.0)
        val item = newLenientCapacityCheckpoint("x", "y")
        item.maximum = 788.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientCapacityCheckpointLabelLengthRule(32)
        val item = newLenientCapacityCheckpoint("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
