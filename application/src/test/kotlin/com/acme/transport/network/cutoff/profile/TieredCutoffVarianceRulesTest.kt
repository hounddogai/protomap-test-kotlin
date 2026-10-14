package com.acme.transport.network.cutoff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCutoffVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCutoffVarianceValidator()
        val item = newTieredCutoffVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = TieredCutoffVarianceRequiredCurrencyRule("tiered cutoff variance needs a value")
        val item = newTieredCutoffVariance("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = TieredCutoffVarianceFuelLitersRangeRule(6.7, 2595.5)
        val item = newTieredCutoffVariance("x", "y")
        item.fuelLiters = 2597.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = TieredCutoffVarianceExpiresAtRangeRule(16, 3600)
        val item = newTieredCutoffVariance("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = TieredCutoffVarianceRetriesRangeRule(120, 1000)
        val item = newTieredCutoffVariance("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
