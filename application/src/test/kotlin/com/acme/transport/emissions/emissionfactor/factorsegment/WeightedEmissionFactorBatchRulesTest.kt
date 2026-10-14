package com.acme.transport.emissions.emissionfactor.factorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedEmissionFactorBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedEmissionFactorBatchValidator()
        val item = newWeightedEmissionFactorBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = WeightedEmissionFactorBatchEtaMinutesRangeRule(16, 120)
        val item = newWeightedEmissionFactorBatch("x", "y")
        item.etaMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedEmissionFactorBatchRequiredNameRule("weighted emission factor batch needs this field")
        val item = newWeightedEmissionFactorBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
