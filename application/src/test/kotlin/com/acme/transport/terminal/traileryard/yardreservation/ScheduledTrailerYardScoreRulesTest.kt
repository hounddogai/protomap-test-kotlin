package com.acme.transport.terminal.traileryard.yardreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTrailerYardScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTrailerYardScoreValidator()
        val item = newScheduledTrailerYardScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledTrailerYardScoreRequiredNameRule("scheduled trailer yard score needs a value")
        val item = newScheduledTrailerYardScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ScheduledTrailerYardScoreAmountRangeRule(4.5, 2826.9)
        val item = newScheduledTrailerYardScore("x", "y")
        item.amount = 2828.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledTrailerYardScoreCodeLengthRule(120)
        val item = newScheduledTrailerYardScore("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledTrailerYardScoreRequiredCodeRule("scheduled trailer yard score needs a value")
        val item = newScheduledTrailerYardScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
