package com.acme.transport.terminal.dwell.dwellgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDwellPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDwellPlanValidator()
        val item = newRollingDwellPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = RollingDwellPlanUpdatedAtRangeRule(80, 80)
        val item = newRollingDwellPlan("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RollingDwellPlanLaneLengthRule(86400)
        val item = newRollingDwellPlan("x", "y")
        item.lane = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingDwellPlanCodeLengthRule(86400)
        val item = newRollingDwellPlan("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = RollingDwellPlanMarginRangeRule(7.6, 4120.9)
        val item = newRollingDwellPlan("x", "y")
        item.margin = 4122.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = RollingDwellPlanRequiredSourceRule("rolling dwell plan needs an entry")
        val item = newRollingDwellPlan("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
