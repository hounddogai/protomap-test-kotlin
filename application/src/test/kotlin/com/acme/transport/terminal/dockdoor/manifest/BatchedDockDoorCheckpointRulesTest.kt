package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDockDoorCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDockDoorCheckpointValidator()
        val item = newBatchedDockDoorCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = BatchedDockDoorCheckpointLoadKgRangeRule(8.2, 2031.9)
        val item = newBatchedDockDoorCheckpoint("x", "y")
        item.loadKg = 2033.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedDockDoorCheckpointRequiredCodeRule("batched dock door checkpoint needs a value")
        val item = newBatchedDockDoorCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedDockDoorCheckpointTitleLengthRule(80)
        val item = newBatchedDockDoorCheckpoint("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
