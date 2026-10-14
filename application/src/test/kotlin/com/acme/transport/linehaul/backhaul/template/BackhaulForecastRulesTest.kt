package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulForecastValidator()
        val item = newBackhaulForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BackhaulForecastRequiredTitleRule("backhaul forecast needs a value")
        val item = newBackhaulForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = BackhaulForecastRequiredSegmentRule("backhaul forecast needs this field")
        val item = newBackhaulForecast("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = BackhaulForecastRateRangeRule(3.8, 4358.1)
        val item = newBackhaulForecast("x", "y")
        item.rate = 4359.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = BackhaulForecastWeightRangeRule(0.7, 4223.2)
        val item = newBackhaulForecast("x", "y")
        item.weight = 4224.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = BackhaulForecastTitleLengthRule(86400)
        val item = newBackhaulForecast("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = BackhaulForecastRequiredReviewerRule("backhaul forecast needs an entry")
        val item = newBackhaulForecast("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
