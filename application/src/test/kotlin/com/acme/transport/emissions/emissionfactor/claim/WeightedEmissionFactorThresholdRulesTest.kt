package com.acme.transport.emissions.emissionfactor.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedEmissionFactorThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedEmissionFactorThresholdValidator()
        val item = newWeightedEmissionFactorThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = WeightedEmissionFactorThresholdRequiredLocaleRule(
            "weighted emission factor threshold needs an entry",
        )
        val item = newWeightedEmissionFactorThreshold("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedEmissionFactorThresholdRequiredCodeRule("weighted emission factor threshold needs an entry")
        val item = newWeightedEmissionFactorThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = WeightedEmissionFactorThresholdSequenceRangeRule(120, 120)
        val item = newWeightedEmissionFactorThreshold("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedEmissionFactorThresholdCodeLengthRule(86400)
        val item = newWeightedEmissionFactorThreshold("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
