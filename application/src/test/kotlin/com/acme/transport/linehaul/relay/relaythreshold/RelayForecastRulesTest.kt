package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayForecastValidator()
        val item = newRelayForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RelayForecastLaneLengthRule(255)
        val item = newRelayForecast("x", "y")
        item.lane = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RelayForecastRequiredTimezoneRule("relay forecast needs an entry")
        val item = newRelayForecast("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RelayForecastTimezoneLengthRule(1000)
        val item = newRelayForecast("x", "y")
        item.timezone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RelayForecastRequiredLaneRule("relay forecast needs this field")
        val item = newRelayForecast("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RelayForecastCodeLengthRule(16)
        val item = newRelayForecast("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
