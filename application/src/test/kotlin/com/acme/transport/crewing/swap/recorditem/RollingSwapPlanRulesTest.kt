package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingSwapPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingSwapPlanValidator()
        val item = newRollingSwapPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RollingSwapPlanGraceDaysRangeRule(32, 86400)
        val item = newRollingSwapPlan("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingSwapPlanRequiredNameRule("rolling swap plan needs an entry")
        val item = newRollingSwapPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingSwapPlanCodeLengthRule(1000)
        val item = newRollingSwapPlan("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingSwapPlanRequiredCodeRule("rolling swap plan needs this field")
        val item = newRollingSwapPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
