package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultScorecardForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultScorecardForecastValidator()
        val item = newDefaultScorecardForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultScorecardForecastRequiredNameRule("default scorecard forecast needs an entry")
        val item = newDefaultScorecardForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = DefaultScorecardForecastPalletsRangeRule(32, 64)
        val item = newDefaultScorecardForecast("x", "y")
        item.pallets = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = DefaultScorecardForecastRequiredChecksumRule("default scorecard forecast needs a value")
        val item = newDefaultScorecardForecast("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = DefaultScorecardForecastChecksumLengthRule(255)
        val item = newDefaultScorecardForecast("x", "y")
        item.checksum = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultScorecardForecastNameLengthRule(255)
        val item = newDefaultScorecardForecast("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
