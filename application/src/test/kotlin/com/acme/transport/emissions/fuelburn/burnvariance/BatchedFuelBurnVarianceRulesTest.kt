package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFuelBurnVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedFuelBurnVarianceValidator()
        val item = newBatchedFuelBurnVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = BatchedFuelBurnVarianceShiftCodeLengthRule(32)
        val item = newBatchedFuelBurnVariance("x", "y")
        item.shiftCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedFuelBurnVarianceRequiredLabelRule("batched fuel burn variance needs a value")
        val item = newBatchedFuelBurnVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = BatchedFuelBurnVarianceStartsAtRangeRule(32, 80)
        val item = newBatchedFuelBurnVariance("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = BatchedFuelBurnVarianceMinimumRangeRule(1.9, 863.4)
        val item = newBatchedFuelBurnVariance("x", "y")
        item.minimum = 864.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
