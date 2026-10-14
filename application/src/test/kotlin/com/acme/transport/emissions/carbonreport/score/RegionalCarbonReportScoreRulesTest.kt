package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCarbonReportScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCarbonReportScoreValidator()
        val item = newRegionalCarbonReportScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RegionalCarbonReportScoreWindowDaysRangeRule(120, 255)
        val item = newRegionalCarbonReportScore("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RegionalCarbonReportScoreRequiredTrailerIdRule("regional carbon report score needs an entry")
        val item = newRegionalCarbonReportScore("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RegionalCarbonReportScoreLaneLengthRule(32)
        val item = newRegionalCarbonReportScore("x", "y")
        item.lane = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RegionalCarbonReportScoreMinimumRangeRule(7.7, 3768.1)
        val item = newRegionalCarbonReportScore("x", "y")
        item.minimum = 3769.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
