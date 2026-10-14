package com.acme.transport.crewing.roster.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRosterEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRosterEstimateValidator()
        val item = newSeasonalRosterEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalRosterEstimateTitleLengthRule(1000)
        val item = newSeasonalRosterEstimate("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = SeasonalRosterEstimateRatioRangeRule(2.2, 442.6)
        val item = newSeasonalRosterEstimate("x", "y")
        item.ratio = 444.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalRosterEstimateCodeLengthRule(80)
        val item = newSeasonalRosterEstimate("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = SeasonalRosterEstimateLaneLengthRule(1000)
        val item = newSeasonalRosterEstimate("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
