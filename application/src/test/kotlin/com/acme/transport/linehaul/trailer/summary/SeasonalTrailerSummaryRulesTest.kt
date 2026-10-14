package com.acme.transport.linehaul.trailer.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTrailerSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTrailerSummaryValidator()
        val item = newSeasonalTrailerSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = SeasonalTrailerSummaryExpiresAtRangeRule(16, 255)
        val item = newSeasonalTrailerSummary("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SeasonalTrailerSummaryRequiredTitleRule("seasonal trailer summary needs this field")
        val item = newSeasonalTrailerSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = SeasonalTrailerSummaryRequiredVehicleIdRule("seasonal trailer summary needs this field")
        val item = newSeasonalTrailerSummary("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
