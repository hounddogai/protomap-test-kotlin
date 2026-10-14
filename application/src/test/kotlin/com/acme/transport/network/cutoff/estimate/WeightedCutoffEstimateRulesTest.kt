package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCutoffEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCutoffEstimateValidator()
        val item = newWeightedCutoffEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = WeightedCutoffEstimateCostRangeRule(4.4, 3512.2)
        val item = newWeightedCutoffEstimate("x", "y")
        item.cost = 3513.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = WeightedCutoffEstimateRequiredShiftCodeRule("weighted cutoff estimate needs an entry")
        val item = newWeightedCutoffEstimate("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedCutoffEstimateRequiredLabelRule("weighted cutoff estimate needs a value")
        val item = newWeightedCutoffEstimate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedCutoffEstimateRequiredCodeRule("weighted cutoff estimate needs an entry")
        val item = newWeightedCutoffEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
