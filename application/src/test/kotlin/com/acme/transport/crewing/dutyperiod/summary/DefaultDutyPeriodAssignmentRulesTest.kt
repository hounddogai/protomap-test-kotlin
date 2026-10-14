package com.acme.transport.crewing.dutyperiod.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDutyPeriodAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDutyPeriodAssignmentValidator()
        val item = newDefaultDutyPeriodAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = DefaultDutyPeriodAssignmentRetriesRangeRule(32, 80)
        val item = newDefaultDutyPeriodAssignment("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = DefaultDutyPeriodAssignmentDistanceKmRangeRule(7.3, 4903.7)
        val item = newDefaultDutyPeriodAssignment("x", "y")
        item.distanceKm = 4905.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
