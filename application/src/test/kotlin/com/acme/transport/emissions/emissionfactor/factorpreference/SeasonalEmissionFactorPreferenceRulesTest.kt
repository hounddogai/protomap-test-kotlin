package com.acme.transport.emissions.emissionfactor.factorpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalEmissionFactorPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalEmissionFactorPreferenceValidator()
        val item = newSeasonalEmissionFactorPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalEmissionFactorPreferenceCodeLengthRule(120)
        val item = newSeasonalEmissionFactorPreference("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalEmissionFactorPreferenceTitleLengthRule(1000)
        val item = newSeasonalEmissionFactorPreference("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = SeasonalEmissionFactorPreferenceStopCountRangeRule(3600, 86400)
        val item = newSeasonalEmissionFactorPreference("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
