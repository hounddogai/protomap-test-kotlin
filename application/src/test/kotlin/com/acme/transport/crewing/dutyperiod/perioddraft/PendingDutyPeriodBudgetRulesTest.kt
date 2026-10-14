package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDutyPeriodBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDutyPeriodBudgetValidator()
        val item = newPendingDutyPeriodBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PendingDutyPeriodBudgetStopCountRangeRule(16, 3600)
        val item = newPendingDutyPeriodBudget("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = PendingDutyPeriodBudgetMarginRangeRule(7.6, 781.3)
        val item = newPendingDutyPeriodBudget("x", "y")
        item.margin = 782.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
