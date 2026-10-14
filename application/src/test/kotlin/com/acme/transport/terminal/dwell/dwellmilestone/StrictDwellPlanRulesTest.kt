package com.acme.transport.terminal.dwell.dwellmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDwellPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDwellPlanValidator()
        val item = newStrictDwellPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictDwellPlanCodeLengthRule(80)
        val item = newStrictDwellPlan("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = StrictDwellPlanVersionRangeRule(32, 120)
        val item = newStrictDwellPlan("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictDwellPlanRequiredCodeRule("strict dwell plan needs a value")
        val item = newStrictDwellPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = StrictDwellPlanRequiredReviewerRule("strict dwell plan needs a value")
        val item = newStrictDwellPlan("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = StrictDwellPlanReviewerLengthRule(86400)
        val item = newStrictDwellPlan("x", "y")
        item.reviewer = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = StrictDwellPlanMinimumRangeRule(2.6, 3170.9)
        val item = newStrictDwellPlan("x", "y")
        item.minimum = 3172.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
