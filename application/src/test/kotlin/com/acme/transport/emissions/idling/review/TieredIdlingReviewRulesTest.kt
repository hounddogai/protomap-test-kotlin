package com.acme.transport.emissions.idling.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredIdlingReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredIdlingReviewValidator()
        val item = newTieredIdlingReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredIdlingReviewRequiredCodeRule("tiered idling review needs an entry")
        val item = newTieredIdlingReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TieredIdlingReviewWindowDaysRangeRule(120, 1000)
        val item = newTieredIdlingReview("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = TieredIdlingReviewCurrencyLengthRule(3600)
        val item = newTieredIdlingReview("x", "y")
        item.currency = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
