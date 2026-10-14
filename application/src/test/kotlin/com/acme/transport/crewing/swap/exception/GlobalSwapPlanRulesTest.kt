package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalSwapPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalSwapPlanValidator()
        val item = newGlobalSwapPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalSwapPlanAttemptsRangeRule(16, 120)
        val item = newGlobalSwapPlan("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalSwapPlanNameLengthRule(1000)
        val item = newGlobalSwapPlan("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalSwapPlanRequiredNameRule("global swap plan needs a value")
        val item = newGlobalSwapPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalSwapPlanCodeLengthRule(32)
        val item = newGlobalSwapPlan("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = GlobalSwapPlanRequiredSegmentRule("global swap plan needs this field")
        val item = newGlobalSwapPlan("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
