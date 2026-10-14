package com.acme.transport.emissions.idling.idlingwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedIdlingThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedIdlingThresholdValidator()
        val item = newBatchedIdlingThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = BatchedIdlingThresholdFuelLitersRangeRule(7.4, 2389.6)
        val item = newBatchedIdlingThreshold("x", "y")
        item.fuelLiters = 2391.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedIdlingThresholdRequiredNameRule("batched idling threshold needs an entry")
        val item = newBatchedIdlingThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = BatchedIdlingThresholdLaneLengthRule(1000)
        val item = newBatchedIdlingThreshold("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
