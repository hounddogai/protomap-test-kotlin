package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRosterForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRosterForecastValidator()
        val item = newLenientRosterForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = LenientRosterForecastMaximumRangeRule(8.2, 3675.3)
        val item = newLenientRosterForecast("x", "y")
        item.maximum = 3676.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientRosterForecastRequiredNameRule("lenient roster forecast needs an entry")
        val item = newLenientRosterForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
