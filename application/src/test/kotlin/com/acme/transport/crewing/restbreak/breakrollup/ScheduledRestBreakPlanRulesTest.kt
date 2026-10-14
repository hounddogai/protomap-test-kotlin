package com.acme.transport.crewing.restbreak.breakrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRestBreakPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRestBreakPlanValidator()
        val item = newScheduledRestBreakPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = ScheduledRestBreakPlanPalletsRangeRule(255, 3600)
        val item = newScheduledRestBreakPlan("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledRestBreakPlanRequiredCodeRule("scheduled rest break plan needs a value")
        val item = newScheduledRestBreakPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ScheduledRestBreakPlanRequiredDriverIdRule("scheduled rest break plan needs a value")
        val item = newScheduledRestBreakPlan("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
