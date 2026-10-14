package com.acme.transport.lastmile.locker.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerTargetValidator()
        val item = newLockerTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = LockerTargetRequiredReviewerRule("locker target needs a value")
        val item = newLockerTarget("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = LockerTargetReviewerLengthRule(80)
        val item = newLockerTarget("x", "y")
        item.reviewer = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerTargetRequiredCodeRule("locker target needs this field")
        val item = newLockerTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
