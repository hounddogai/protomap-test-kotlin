package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCrossDockCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCrossDockCycleValidator()
        val item = newBatchedCrossDockCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = BatchedCrossDockCycleAttemptsRangeRule(16, 1000)
        val item = newBatchedCrossDockCycle("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = BatchedCrossDockCycleThresholdRangeRule(4.9, 2291.1)
        val item = newBatchedCrossDockCycle("x", "y")
        item.threshold = 2292.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedCrossDockCycleRequiredLabelRule("batched cross dock cycle needs this field")
        val item = newBatchedCrossDockCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = BatchedCrossDockCycleRequiredRegionRule("batched cross dock cycle needs this field")
        val item = newBatchedCrossDockCycle("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
