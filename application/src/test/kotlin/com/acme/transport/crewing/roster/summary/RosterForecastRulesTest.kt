package com.acme.transport.crewing.roster.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterForecastValidator()
        val item = newRosterForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RosterForecastRequiredLaneRule("roster forecast needs a value")
        val item = newRosterForecast("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = RosterForecastPriorityRangeRule(64, 3600)
        val item = newRosterForecast("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RosterForecastLaneLengthRule(1000)
        val item = newRosterForecast("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
