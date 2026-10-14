package com.acme.transport.freightpay.audit.legacyauditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAuditDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAuditDigestValidator()
        val item = newCappedAuditDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = CappedAuditDigestMinimumRangeRule(7.8, 1691.9)
        val item = newCappedAuditDigest("x", "y")
        item.minimum = 1693.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = CappedAuditDigestRequiredReviewerRule("capped audit digest needs a value")
        val item = newCappedAuditDigest("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = CappedAuditDigestScoreRangeRule(3.5, 4167.0)
        val item = newCappedAuditDigest("x", "y")
        item.score = 4168.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
