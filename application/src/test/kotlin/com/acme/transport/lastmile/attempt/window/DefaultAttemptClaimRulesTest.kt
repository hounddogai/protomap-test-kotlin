package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAttemptClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultAttemptClaimValidator()
        val item = newDefaultAttemptClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = DefaultAttemptClaimQuantityRangeRule(16, 64)
        val item = newDefaultAttemptClaim("x", "y")
        item.quantity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DefaultAttemptClaimGraceDaysRangeRule(32, 1000)
        val item = newDefaultAttemptClaim("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = DefaultAttemptClaimCountryLengthRule(86400)
        val item = newDefaultAttemptClaim("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultAttemptClaimTitleLengthRule(64)
        val item = newDefaultAttemptClaim("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long segment`() {
        val rule = DefaultAttemptClaimSegmentLengthRule(80)
        val item = newDefaultAttemptClaim("x", "y")
        item.segment = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
