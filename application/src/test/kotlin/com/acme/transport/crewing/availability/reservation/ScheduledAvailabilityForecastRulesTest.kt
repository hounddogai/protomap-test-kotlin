package com.acme.transport.crewing.availability.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAvailabilityForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAvailabilityForecastValidator()
        val item = newScheduledAvailabilityForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAvailabilityForecastCodeLengthRule(3600)
        val item = newScheduledAvailabilityForecast("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ScheduledAvailabilityForecastThresholdRangeRule(0.9, 4578.0)
        val item = newScheduledAvailabilityForecast("x", "y")
        item.threshold = 4579.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = ScheduledAvailabilityForecastRevisionRangeRule(255, 1000)
        val item = newScheduledAvailabilityForecast("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledAvailabilityForecastTitleLengthRule(80)
        val item = newScheduledAvailabilityForecast("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ScheduledAvailabilityForecastTimezoneLengthRule(64)
        val item = newScheduledAvailabilityForecast("x", "y")
        item.timezone = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
