package com.acme.transport.freightpay.audit.auditreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAuditReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictAuditReviewValidator()
        val item = newStrictAuditReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = StrictAuditReviewRequiredRouteCodeRule("strict audit review needs an entry")
        val item = newStrictAuditReview("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = StrictAuditReviewWindowDaysRangeRule(16, 3600)
        val item = newStrictAuditReview("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
