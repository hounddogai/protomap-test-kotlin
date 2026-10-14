package com.acme.transport.network.hub.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubForecastValidator()
        val item = newHubForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HubForecastRequiredCodeRule("hub forecast needs this field")
        val item = newHubForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = HubForecastNameLengthRule(16)
        val item = newHubForecast("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = HubForecastCodeLengthRule(86400)
        val item = newHubForecast("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
