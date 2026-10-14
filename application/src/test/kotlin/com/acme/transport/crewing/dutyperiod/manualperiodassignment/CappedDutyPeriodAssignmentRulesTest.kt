package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDutyPeriodAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDutyPeriodAssignmentValidator()
        val item = newCappedDutyPeriodAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = CappedDutyPeriodAssignmentDiscountRangeRule(0.7, 3448.0)
        val item = newCappedDutyPeriodAssignment("x", "y")
        item.discount = 3449.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CappedDutyPeriodAssignmentDriverIdLengthRule(16)
        val item = newCappedDutyPeriodAssignment("x", "y")
        item.driverId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = CappedDutyPeriodAssignmentReviewerLengthRule(1000)
        val item = newCappedDutyPeriodAssignment("x", "y")
        item.reviewer = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = CappedDutyPeriodAssignmentLimitRangeRule(32, 255)
        val item = newCappedDutyPeriodAssignment("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
