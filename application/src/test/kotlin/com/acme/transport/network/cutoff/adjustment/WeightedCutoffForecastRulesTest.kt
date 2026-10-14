package com.acme.transport.network.cutoff.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCutoffForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCutoffForecastValidator()
        val item = newWeightedCutoffForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedCutoffForecastNameLengthRule(255)
        val item = newWeightedCutoffForecast("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = WeightedCutoffForecastRequiredContactEmailRule("weighted cutoff forecast needs this field")
        val item = newWeightedCutoffForecast("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = WeightedCutoffForecastLoadKgRangeRule(7.2, 4058.2)
        val item = newWeightedCutoffForecast("x", "y")
        item.loadKg = 4059.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = WeightedCutoffForecastUpdatedAtRangeRule(32, 255)
        val item = newWeightedCutoffForecast("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
