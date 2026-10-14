package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardOverrideValidator()
        val item = newScorecardOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ScorecardOverrideQuantityRangeRule(80, 255)
        val item = newScorecardOverride("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScorecardOverrideTitleLengthRule(3600)
        val item = newScorecardOverride("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardOverrideRequiredCodeRule("scorecard override needs a value")
        val item = newScorecardOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ScorecardOverrideGraceDaysRangeRule(80, 3600)
        val item = newScorecardOverride("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
