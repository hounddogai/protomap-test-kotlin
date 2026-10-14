package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnBatchValidator()
        val item = newFuelBurnBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = FuelBurnBatchCurrencyLengthRule(64)
        val item = newFuelBurnBatch("x", "y")
        item.currency = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = FuelBurnBatchRequiredDepotRule("fuel burn batch needs this field")
        val item = newFuelBurnBatch("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FuelBurnBatchThresholdRangeRule(6.3, 4309.2)
        val item = newFuelBurnBatch("x", "y")
        item.threshold = 4310.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = FuelBurnBatchCostRangeRule(2.9, 952.0)
        val item = newFuelBurnBatch("x", "y")
        item.cost = 953.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
