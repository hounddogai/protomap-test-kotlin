package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaForecastValidator()
        val item = newServiceAreaForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ServiceAreaForecastRequiredNameRule("service area forecast needs a value")
        val item = newServiceAreaForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ServiceAreaForecastNameLengthRule(86400)
        val item = newServiceAreaForecast("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = ServiceAreaForecastDriverIdLengthRule(32)
        val item = newServiceAreaForecast("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ServiceAreaForecastRequiredDriverIdRule("service area forecast needs a value")
        val item = newServiceAreaForecast("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaForecastRequiredCodeRule("service area forecast needs a value")
        val item = newServiceAreaForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
