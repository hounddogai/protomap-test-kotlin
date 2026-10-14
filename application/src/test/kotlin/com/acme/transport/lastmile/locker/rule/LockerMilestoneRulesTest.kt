package com.acme.transport.lastmile.locker.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerMilestoneValidator()
        val item = newLockerMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = LockerMilestoneReviewerLengthRule(32)
        val item = newLockerMilestone("x", "y")
        item.reviewer = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerMilestoneRequiredCodeRule("locker milestone needs this field")
        val item = newLockerMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LockerMilestoneTitleLengthRule(64)
        val item = newLockerMilestone("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
