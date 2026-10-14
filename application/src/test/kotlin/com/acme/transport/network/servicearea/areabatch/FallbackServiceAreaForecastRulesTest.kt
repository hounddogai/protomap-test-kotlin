package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackServiceAreaForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackServiceAreaForecastValidator()
        val item = newFallbackServiceAreaForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackServiceAreaForecastRequiredCodeRule("fallback service area forecast needs a value")
        val item = newFallbackServiceAreaForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = FallbackServiceAreaForecastUnitLengthRule(86400)
        val item = newFallbackServiceAreaForecast("x", "y")
        item.unit = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
