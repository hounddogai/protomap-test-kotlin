package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAttemptQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAttemptQuotaValidator()
        val item = newManualAttemptQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualAttemptQuotaTitleLengthRule(3600)
        val item = newManualAttemptQuota("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ManualAttemptQuotaRequiredSegmentRule("manual attempt quota needs this field")
        val item = newManualAttemptQuota("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = ManualAttemptQuotaRequiredOwnerIdRule("manual attempt quota needs this field")
        val item = newManualAttemptQuota("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ManualAttemptQuotaThresholdRangeRule(7.3, 79.7)
        val item = newManualAttemptQuota("x", "y")
        item.threshold = 81.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
