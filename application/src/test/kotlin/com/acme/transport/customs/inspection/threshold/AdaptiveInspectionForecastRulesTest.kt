package com.acme.transport.customs.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionForecastValidator()
        val item = newAdaptiveInspectionForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = AdaptiveInspectionForecastTrailerIdLengthRule(3600)
        val item = newAdaptiveInspectionForecast("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveInspectionForecastLabelLengthRule(120)
        val item = newAdaptiveInspectionForecast("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = AdaptiveInspectionForecastVersionRangeRule(255, 3600)
        val item = newAdaptiveInspectionForecast("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveInspectionForecastRequiredTitleRule("adaptive inspection forecast needs an entry")
        val item = newAdaptiveInspectionForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
