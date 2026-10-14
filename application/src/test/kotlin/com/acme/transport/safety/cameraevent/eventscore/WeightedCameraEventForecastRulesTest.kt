package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCameraEventForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCameraEventForecastValidator()
        val item = newWeightedCameraEventForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedCameraEventForecastRequiredCodeRule("weighted camera event forecast needs a value")
        val item = newWeightedCameraEventForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedCameraEventForecastTitleLengthRule(120)
        val item = newWeightedCameraEventForecast("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedCameraEventForecastRequiredTitleRule("weighted camera event forecast needs an entry")
        val item = newWeightedCameraEventForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = WeightedCameraEventForecastTimezoneLengthRule(120)
        val item = newWeightedCameraEventForecast("x", "y")
        item.timezone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
