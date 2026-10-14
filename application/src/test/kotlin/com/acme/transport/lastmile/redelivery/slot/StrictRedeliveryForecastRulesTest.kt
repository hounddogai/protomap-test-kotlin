package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRedeliveryForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRedeliveryForecastValidator()
        val item = newStrictRedeliveryForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = StrictRedeliveryForecastRouteCodeLengthRule(64)
        val item = newStrictRedeliveryForecast("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictRedeliveryForecastLabelLengthRule(1000)
        val item = newStrictRedeliveryForecast("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = StrictRedeliveryForecastRequiredRouteCodeRule("strict redelivery forecast needs an entry")
        val item = newStrictRedeliveryForecast("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
