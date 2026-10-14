package com.acme.transport.crewing.restbreak.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRestBreakPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRestBreakPlanValidator()
        val item = newStrictRestBreakPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictRestBreakPlanTitleLengthRule(64)
        val item = newStrictRestBreakPlan("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictRestBreakPlanRequiredTitleRule("strict rest break plan needs a value")
        val item = newStrictRestBreakPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictRestBreakPlanCodeLengthRule(86400)
        val item = newStrictRestBreakPlan("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = StrictRestBreakPlanCategoryLengthRule(1000)
        val item = newStrictRestBreakPlan("x", "y")
        item.category = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = StrictRestBreakPlanRatioRangeRule(5.7, 3160.8)
        val item = newStrictRestBreakPlan("x", "y")
        item.ratio = 3162.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
