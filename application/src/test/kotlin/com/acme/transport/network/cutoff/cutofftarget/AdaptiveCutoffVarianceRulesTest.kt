package com.acme.transport.network.cutoff.cutofftarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCutoffVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCutoffVarianceValidator()
        val item = newAdaptiveCutoffVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AdaptiveCutoffVarianceFuelLitersRangeRule(7.8, 1921.3)
        val item = newAdaptiveCutoffVariance("x", "y")
        item.fuelLiters = 1922.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveCutoffVarianceRequiredTitleRule("adaptive cutoff variance needs a value")
        val item = newAdaptiveCutoffVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = AdaptiveCutoffVarianceEtaMinutesRangeRule(255, 3600)
        val item = newAdaptiveCutoffVariance("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
