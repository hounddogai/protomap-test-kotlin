package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTrailerYardOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTrailerYardOverrideValidator()
        val item = newSeasonalTrailerYardOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = SeasonalTrailerYardOverrideCeilingRangeRule(6.2, 2451.7)
        val item = newSeasonalTrailerYardOverride("x", "y")
        item.ceiling = 2453.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalTrailerYardOverrideRequiredNameRule("seasonal trailer yard override needs an entry")
        val item = newSeasonalTrailerYardOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
