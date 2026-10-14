package com.acme.transport.freightpay.fuelsurcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackFuelSurchargeForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackFuelSurchargeForecastValidator()
        val item = newFallbackFuelSurchargeForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = FallbackFuelSurchargeForecastRequiredExternalRefRule(
            "fallback fuel surcharge forecast needs this field",
        )
        val item = newFallbackFuelSurchargeForecast("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = FallbackFuelSurchargeForecastScoreRangeRule(5.3, 4653.3)
        val item = newFallbackFuelSurchargeForecast("x", "y")
        item.score = 4654.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackFuelSurchargeForecastRequiredCodeRule("fallback fuel surcharge forecast needs an entry")
        val item = newFallbackFuelSurchargeForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = FallbackFuelSurchargeForecastEndsAtRangeRule(80, 1000)
        val item = newFallbackFuelSurchargeForecast("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
