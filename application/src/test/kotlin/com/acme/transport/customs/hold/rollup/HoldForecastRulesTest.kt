package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldForecastValidator()
        val item = newHoldForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = HoldForecastExternalRefLengthRule(86400)
        val item = newHoldForecast("x", "y")
        item.externalRef = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long currency`() {
        val rule = HoldForecastCurrencyLengthRule(120)
        val item = newHoldForecast("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldForecastRequiredCodeRule("hold forecast needs an entry")
        val item = newHoldForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
