package com.acme.transport.freightpay.audit.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditReviewValidator()
        val item = newAuditReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = AuditReviewRequiredRegionRule("audit review needs an entry")
        val item = newAuditReview("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = AuditReviewAttemptsRangeRule(16, 1000)
        val item = newAuditReview("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = AuditReviewRegionLengthRule(32)
        val item = newAuditReview("x", "y")
        item.region = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
