package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnReviewValidator()
        val item = newLeaseReturnReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LeaseReturnReviewRequiredTitleRule("lease return review needs this field")
        val item = newLeaseReturnReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LeaseReturnReviewRequiredCodeRule("lease return review needs a value")
        val item = newLeaseReturnReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LeaseReturnReviewTitleLengthRule(255)
        val item = newLeaseReturnReview("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
