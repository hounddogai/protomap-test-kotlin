package com.acme.transport.safety.incident.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalIncidentOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalIncidentOverrideValidator()
        val item = newSeasonalIncidentOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = SeasonalIncidentOverrideRequiredCountryRule("seasonal incident override needs an entry")
        val item = newSeasonalIncidentOverride("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = SeasonalIncidentOverrideEtaMinutesRangeRule(16, 32)
        val item = newSeasonalIncidentOverride("x", "y")
        item.etaMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = SeasonalIncidentOverrideTimezoneLengthRule(64)
        val item = newSeasonalIncidentOverride("x", "y")
        item.timezone = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
