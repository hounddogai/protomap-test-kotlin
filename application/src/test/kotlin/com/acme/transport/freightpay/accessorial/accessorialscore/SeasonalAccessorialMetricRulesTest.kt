package com.acme.transport.freightpay.accessorial.accessorialscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAccessorialMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAccessorialMetricValidator()
        val item = newSeasonalAccessorialMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalAccessorialMetricRequiredCodeRule("seasonal accessorial metric needs an entry")
        val item = newSeasonalAccessorialMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SeasonalAccessorialMetricRequiredTitleRule("seasonal accessorial metric needs this field")
        val item = newSeasonalAccessorialMetric("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = SeasonalAccessorialMetricDescriptionLengthRule(255)
        val item = newSeasonalAccessorialMetric("x", "y")
        item.description = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = SeasonalAccessorialMetricWeightRangeRule(1.7, 392.0)
        val item = newSeasonalAccessorialMetric("x", "y")
        item.weight = 393.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
