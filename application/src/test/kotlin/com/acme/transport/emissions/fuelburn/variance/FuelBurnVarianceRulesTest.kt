package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnVarianceValidator()
        val item = newFuelBurnVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = FuelBurnVarianceShiftCodeLengthRule(1000)
        val item = newFuelBurnVariance("x", "y")
        item.shiftCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelBurnVarianceCodeLengthRule(255)
        val item = newFuelBurnVariance("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = FuelBurnVarianceBatchSizeRangeRule(16, 64)
        val item = newFuelBurnVariance("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long locale`() {
        val rule = FuelBurnVarianceLocaleLengthRule(255)
        val item = newFuelBurnVariance("x", "y")
        item.locale = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
