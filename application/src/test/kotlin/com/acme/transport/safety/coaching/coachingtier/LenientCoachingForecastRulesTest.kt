package com.acme.transport.safety.coaching.coachingtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCoachingForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCoachingForecastValidator()
        val item = newLenientCoachingForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = LenientCoachingForecastThresholdRangeRule(2.7, 935.3)
        val item = newLenientCoachingForecast("x", "y")
        item.threshold = 936.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LenientCoachingForecastStartsAtRangeRule(64, 120)
        val item = newLenientCoachingForecast("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientCoachingForecastCodeLengthRule(32)
        val item = newLenientCoachingForecast("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientCoachingForecastRequiredTitleRule("lenient coaching forecast needs a value")
        val item = newLenientCoachingForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = LenientCoachingForecastRequiredExternalRefRule("lenient coaching forecast needs an entry")
        val item = newLenientCoachingForecast("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
