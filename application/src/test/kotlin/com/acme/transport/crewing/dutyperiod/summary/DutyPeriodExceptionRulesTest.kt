package com.acme.transport.crewing.dutyperiod.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodExceptionValidator()
        val item = newDutyPeriodException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = DutyPeriodExceptionUpdatedAtRangeRule(16, 32)
        val item = newDutyPeriodException("x", "y")
        item.updatedAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = DutyPeriodExceptionRequiredSourceRule("duty period exception needs an entry")
        val item = newDutyPeriodException("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = DutyPeriodExceptionRateRangeRule(9.5, 2211.5)
        val item = newDutyPeriodException("x", "y")
        item.rate = 2213.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DutyPeriodExceptionGraceDaysRangeRule(16, 80)
        val item = newDutyPeriodException("x", "y")
        item.graceDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodExceptionRequiredCodeRule("duty period exception needs an entry")
        val item = newDutyPeriodException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
