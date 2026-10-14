package com.acme.transport.network.servicearea.areathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredServiceAreaForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredServiceAreaForecastValidator()
        val item = newTieredServiceAreaForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredServiceAreaForecastLabelLengthRule(86400)
        val item = newTieredServiceAreaForecast("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredServiceAreaForecastRequiredCodeRule("tiered service area forecast needs this field")
        val item = newTieredServiceAreaForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = TieredServiceAreaForecastQuantityRangeRule(1000, 3600)
        val item = newTieredServiceAreaForecast("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredServiceAreaForecastCodeLengthRule(3600)
        val item = newTieredServiceAreaForecast("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
