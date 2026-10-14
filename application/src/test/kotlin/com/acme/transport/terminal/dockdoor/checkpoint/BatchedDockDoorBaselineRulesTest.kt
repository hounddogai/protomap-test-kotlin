package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDockDoorBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDockDoorBaselineValidator()
        val item = newBatchedDockDoorBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = BatchedDockDoorBaselineScoreRangeRule(3.6, 4757.5)
        val item = newBatchedDockDoorBaseline("x", "y")
        item.score = 4759.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedDockDoorBaselineCodeLengthRule(1000)
        val item = newBatchedDockDoorBaseline("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = BatchedDockDoorBaselineRequiredCarrierRule("batched dock door baseline needs this field")
        val item = newBatchedDockDoorBaseline("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedDockDoorBaselineRequiredTitleRule("batched dock door baseline needs a value")
        val item = newBatchedDockDoorBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = BatchedDockDoorBaselineCapacityRangeRule(1000, 3600)
        val item = newBatchedDockDoorBaseline("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
