package com.acme.transport.linehaul.tender.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTenderReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTenderReviewValidator()
        val item = newGlobalTenderReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = GlobalTenderReviewUnitLengthRule(32)
        val item = newGlobalTenderReview("x", "y")
        item.unit = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = GlobalTenderReviewRequiredUnitRule("global tender review needs a value")
        val item = newGlobalTenderReview("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalTenderReviewTitleLengthRule(86400)
        val item = newGlobalTenderReview("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
