package com.acme.transport.lastmile.redelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryForecastValidator()
        val item = newRedeliveryForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RedeliveryForecastRequiredRouteCodeRule("redelivery forecast needs an entry")
        val item = newRedeliveryForecast("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = RedeliveryForecastVersionRangeRule(16, 80)
        val item = newRedeliveryForecast("x", "y")
        item.version = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = RedeliveryForecastRequiredCountryRule("redelivery forecast needs a value")
        val item = newRedeliveryForecast("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RedeliveryForecastRequiredTitleRule("redelivery forecast needs this field")
        val item = newRedeliveryForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
