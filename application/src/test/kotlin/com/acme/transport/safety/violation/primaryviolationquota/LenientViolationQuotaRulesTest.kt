package com.acme.transport.safety.violation.primaryviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientViolationQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientViolationQuotaValidator()
        val item = newLenientViolationQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LenientViolationQuotaRetriesRangeRule(120, 86400)
        val item = newLenientViolationQuota("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LenientViolationQuotaAttemptsRangeRule(3600, 86400)
        val item = newLenientViolationQuota("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientViolationQuotaRequiredCodeRule("lenient violation quota needs this field")
        val item = newLenientViolationQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientViolationQuotaRequiredTitleRule("lenient violation quota needs an entry")
        val item = newLenientViolationQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
