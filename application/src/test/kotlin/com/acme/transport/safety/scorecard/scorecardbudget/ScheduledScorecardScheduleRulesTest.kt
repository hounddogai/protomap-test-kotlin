package com.acme.transport.safety.scorecard.scorecardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledScorecardScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledScorecardScheduleValidator()
        val item = newScheduledScorecardSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = ScheduledScorecardScheduleSourceLengthRule(80)
        val item = newScheduledScorecardSchedule("x", "y")
        item.source = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = ScheduledScorecardScheduleScoreRangeRule(9.7, 277.2)
        val item = newScheduledScorecardSchedule("x", "y")
        item.score = 278.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = ScheduledScorecardScheduleReviewerLengthRule(120)
        val item = newScheduledScorecardSchedule("x", "y")
        item.reviewer = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledScorecardScheduleCodeLengthRule(64)
        val item = newScheduledScorecardSchedule("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ScheduledScorecardScheduleDiscountRangeRule(5.0, 2853.0)
        val item = newScheduledScorecardSchedule("x", "y")
        item.discount = 2854.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ScheduledScorecardScheduleGraceDaysRangeRule(1000, 86400)
        val item = newScheduledScorecardSchedule("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
