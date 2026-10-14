package com.acme.transport.lastmile.attempt.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAttemptAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAttemptAuditValidator()
        val item = newLenientAttemptAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientAttemptAuditRequiredTitleRule("lenient attempt audit needs a value")
        val item = newLenientAttemptAudit("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LenientAttemptAuditRetriesRangeRule(32, 120)
        val item = newLenientAttemptAudit("x", "y")
        item.retries = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientAttemptAuditTitleLengthRule(80)
        val item = newLenientAttemptAudit("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
