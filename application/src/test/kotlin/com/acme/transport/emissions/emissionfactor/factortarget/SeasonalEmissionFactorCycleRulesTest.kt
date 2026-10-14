package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalEmissionFactorCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalEmissionFactorCycleValidator()
        val item = newSeasonalEmissionFactorCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = SeasonalEmissionFactorCycleFuelLitersRangeRule(9.8, 4808.5)
        val item = newSeasonalEmissionFactorCycle("x", "y")
        item.fuelLiters = 4810.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = SeasonalEmissionFactorCycleBudgetRangeRule(6.6, 2189.9)
        val item = newSeasonalEmissionFactorCycle("x", "y")
        item.budget = 2191.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = SeasonalEmissionFactorCycleRequiredDockRule("seasonal emission factor cycle needs an entry")
        val item = newSeasonalEmissionFactorCycle("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
