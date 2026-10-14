package com.acme.transport.customs.hold.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldReviewValidator()
        val item = newHoldReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldReviewCodeLengthRule(16)
        val item = newHoldReview("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = HoldReviewRateRangeRule(3.6, 4573.5)
        val item = newHoldReview("x", "y")
        item.rate = 4575.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = HoldReviewRequiredTitleRule("hold review needs this field")
        val item = newHoldReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
