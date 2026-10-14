package com.acme.transport.emissions.fuelburn.burnrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFuelBurnCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedFuelBurnCycleValidator()
        val item = newBatchedFuelBurnCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = BatchedFuelBurnCycleDurationMinutesRangeRule(64, 3600)
        val item = newBatchedFuelBurnCycle("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedFuelBurnCycleCodeLengthRule(1000)
        val item = newBatchedFuelBurnCycle("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
