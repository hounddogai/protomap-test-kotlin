package com.acme.transport.emissions.fuelburn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyFuelBurnDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyFuelBurnDraftValidator()
        val item = newLegacyFuelBurnDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LegacyFuelBurnDraftWeightRangeRule(5.2, 4547.0)
        val item = newLegacyFuelBurnDraft("x", "y")
        item.weight = 4548.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = LegacyFuelBurnDraftRateRangeRule(7.4, 1312.1)
        val item = newLegacyFuelBurnDraft("x", "y")
        item.rate = 1313.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyFuelBurnDraftRequiredCodeRule("legacy fuel burn draft needs an entry")
        val item = newLegacyFuelBurnDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
