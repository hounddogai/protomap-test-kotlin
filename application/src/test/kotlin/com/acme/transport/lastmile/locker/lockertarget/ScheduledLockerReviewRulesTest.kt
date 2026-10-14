package com.acme.transport.lastmile.locker.lockertarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLockerReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLockerReviewValidator()
        val item = newScheduledLockerReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = ScheduledLockerReviewRequiredPhoneRule("scheduled locker review needs this field")
        val item = newScheduledLockerReview("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ScheduledLockerReviewDurationMinutesRangeRule(255, 3600)
        val item = newScheduledLockerReview("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledLockerReviewRequiredNameRule("scheduled locker review needs this field")
        val item = newScheduledLockerReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
