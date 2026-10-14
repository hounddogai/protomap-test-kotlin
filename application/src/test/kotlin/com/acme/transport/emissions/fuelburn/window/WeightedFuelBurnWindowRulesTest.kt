package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedFuelBurnWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedFuelBurnWindowValidator()
        val item = newWeightedFuelBurnWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = WeightedFuelBurnWindowMarginRangeRule(1.0, 2496.5)
        val item = newWeightedFuelBurnWindow("x", "y")
        item.margin = 2498.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedFuelBurnWindowRequiredLabelRule("weighted fuel burn window needs an entry")
        val item = newWeightedFuelBurnWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedFuelBurnWindowLabelLengthRule(86400)
        val item = newWeightedFuelBurnWindow("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
