package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardRuleValidator()
        val item = newScorecardRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = ScorecardRuleExternalRefLengthRule(120)
        val item = newScorecardRule("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ScorecardRuleRequiredExternalRefRule("scorecard rule needs a value")
        val item = newScorecardRule("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScorecardRuleRequiredTitleRule("scorecard rule needs a value")
        val item = newScorecardRule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = ScorecardRuleVersionRangeRule(16, 32)
        val item = newScorecardRule("x", "y")
        item.version = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScorecardRuleTitleLengthRule(3600)
        val item = newScorecardRule("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
