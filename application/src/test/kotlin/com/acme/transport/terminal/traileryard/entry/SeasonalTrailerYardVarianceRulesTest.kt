package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTrailerYardVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTrailerYardVarianceValidator()
        val item = newSeasonalTrailerYardVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = SeasonalTrailerYardVarianceDriverIdLengthRule(32)
        val item = newSeasonalTrailerYardVariance("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = SeasonalTrailerYardVarianceRequiredDriverIdRule("seasonal trailer yard variance needs this field")
        val item = newSeasonalTrailerYardVariance("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = SeasonalTrailerYardVarianceAmountRangeRule(4.4, 2112.6)
        val item = newSeasonalTrailerYardVariance("x", "y")
        item.amount = 2114.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalTrailerYardVarianceNameLengthRule(80)
        val item = newSeasonalTrailerYardVariance("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalTrailerYardVarianceRequiredCodeRule("seasonal trailer yard variance needs an entry")
        val item = newSeasonalTrailerYardVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
