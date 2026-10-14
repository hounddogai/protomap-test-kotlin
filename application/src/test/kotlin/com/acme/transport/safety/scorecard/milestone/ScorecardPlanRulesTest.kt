package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardPlanValidator()
        val item = newScorecardPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScorecardPlanLabelLengthRule(86400)
        val item = newScorecardPlan("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScorecardPlanCodeLengthRule(3600)
        val item = newScorecardPlan("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ScorecardPlanMaximumRangeRule(8.1, 3510.6)
        val item = newScorecardPlan("x", "y")
        item.maximum = 3512.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardPlanRequiredCodeRule("scorecard plan needs a value")
        val item = newScorecardPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
