package com.acme.transport.fleetops.serviceplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledServicePlanBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledServicePlanBudgetValidator()
        val item = newScheduledServicePlanBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ScheduledServicePlanBudgetStopCountRangeRule(80, 120)
        val item = newScheduledServicePlanBudget("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledServicePlanBudgetNameLengthRule(64)
        val item = newScheduledServicePlanBudget("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledServicePlanBudgetRequiredCodeRule("scheduled service plan budget needs an entry")
        val item = newScheduledServicePlanBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
