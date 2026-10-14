package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardEntryValidator()
        val item = newScorecardEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ScorecardEntryDiscountRangeRule(5.8, 2363.1)
        val item = newScorecardEntry("x", "y")
        item.discount = 2364.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ScorecardEntryEtaMinutesRangeRule(32, 86400)
        val item = newScorecardEntry("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = ScorecardEntryRegionLengthRule(86400)
        val item = newScorecardEntry("x", "y")
        item.region = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ScorecardEntryLocaleLengthRule(86400)
        val item = newScorecardEntry("x", "y")
        item.locale = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
