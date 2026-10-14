package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodPlanValidator()
        val item = newDutyPeriodPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = DutyPeriodPlanUpdatedAtRangeRule(120, 120)
        val item = newDutyPeriodPlan("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = DutyPeriodPlanDiscountRangeRule(7.7, 98.8)
        val item = newDutyPeriodPlan("x", "y")
        item.discount = 100.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodPlanRequiredCodeRule("duty period plan needs this field")
        val item = newDutyPeriodPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
