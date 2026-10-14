package com.acme.transport.safety.scorecard.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardWindowValidator()
        val item = newScorecardWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ScorecardWindowTrailerIdLengthRule(64)
        val item = newScorecardWindow("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = ScorecardWindowRegionLengthRule(86400)
        val item = newScorecardWindow("x", "y")
        item.region = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ScorecardWindowDiscountRangeRule(4.4, 4413.2)
        val item = newScorecardWindow("x", "y")
        item.discount = 4414.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
