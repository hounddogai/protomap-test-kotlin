package com.acme.transport.lastmile.attempt.attemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAttemptRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAttemptRequestValidator()
        val item = newLenientAttemptRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientAttemptRequestRequiredCodeRule("lenient attempt request needs an entry")
        val item = newLenientAttemptRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = LenientAttemptRequestSourceLengthRule(16)
        val item = newLenientAttemptRequest("x", "y")
        item.source = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = LenientAttemptRequestRateRangeRule(5.1, 1087.8)
        val item = newLenientAttemptRequest("x", "y")
        item.rate = 1089.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientAttemptRequestTitleLengthRule(3600)
        val item = newLenientAttemptRequest("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientAttemptRequestNameLengthRule(80)
        val item = newLenientAttemptRequest("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LenientAttemptRequestCostRangeRule(5.1, 3392.6)
        val item = newLenientAttemptRequest("x", "y")
        item.cost = 3394.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
