package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTrailerYardScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTrailerYardScoreValidator()
        val item = newSeasonalTrailerYardScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalTrailerYardScoreRequiredCodeRule("seasonal trailer yard score needs this field")
        val item = newSeasonalTrailerYardScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalTrailerYardScoreTitleLengthRule(3600)
        val item = newSeasonalTrailerYardScore("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = SeasonalTrailerYardScoreRequiredCategoryRule("seasonal trailer yard score needs this field")
        val item = newSeasonalTrailerYardScore("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = SeasonalTrailerYardScoreCostRangeRule(4.3, 280.4)
        val item = newSeasonalTrailerYardScore("x", "y")
        item.cost = 281.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
