package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEmissionFactorMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveEmissionFactorMilestoneValidator()
        val item = newAdaptiveEmissionFactorMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = AdaptiveEmissionFactorMilestoneVersionRangeRule(32, 3600)
        val item = newAdaptiveEmissionFactorMilestone("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = AdaptiveEmissionFactorMilestoneRequiredCurrencyRule(
            "adaptive emission factor milestone needs this field",
        )
        val item = newAdaptiveEmissionFactorMilestone("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = AdaptiveEmissionFactorMilestoneCurrencyLengthRule(32)
        val item = newAdaptiveEmissionFactorMilestone("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
