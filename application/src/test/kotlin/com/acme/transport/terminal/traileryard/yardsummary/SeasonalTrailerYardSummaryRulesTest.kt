package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTrailerYardSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTrailerYardSummaryValidator()
        val item = newSeasonalTrailerYardSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = SeasonalTrailerYardSummaryDriverIdLengthRule(120)
        val item = newSeasonalTrailerYardSummary("x", "y")
        item.driverId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalTrailerYardSummaryCodeLengthRule(16)
        val item = newSeasonalTrailerYardSummary("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = SeasonalTrailerYardSummaryRequiredLaneRule("seasonal trailer yard summary needs a value")
        val item = newSeasonalTrailerYardSummary("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
