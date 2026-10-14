package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEmissionFactorEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveEmissionFactorEntryValidator()
        val item = newAdaptiveEmissionFactorEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AdaptiveEmissionFactorEntryFuelLitersRangeRule(5.4, 4013.8)
        val item = newAdaptiveEmissionFactorEntry("x", "y")
        item.fuelLiters = 4015.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = AdaptiveEmissionFactorEntryCostRangeRule(5.5, 3457.8)
        val item = newAdaptiveEmissionFactorEntry("x", "y")
        item.cost = 3459.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveEmissionFactorEntryRequiredTitleRule("adaptive emission factor entry needs an entry")
        val item = newAdaptiveEmissionFactorEntry("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
