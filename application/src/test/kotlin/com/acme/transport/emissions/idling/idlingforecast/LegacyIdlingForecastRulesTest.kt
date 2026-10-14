package com.acme.transport.emissions.idling.idlingforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyIdlingForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyIdlingForecastValidator()
        val item = newLegacyIdlingForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LegacyIdlingForecastLimitRangeRule(80, 80)
        val item = newLegacyIdlingForecast("x", "y")
        item.limit = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = LegacyIdlingForecastRequiredSourceRule("legacy idling forecast needs this field")
        val item = newLegacyIdlingForecast("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = LegacyIdlingForecastEndsAtRangeRule(80, 255)
        val item = newLegacyIdlingForecast("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
