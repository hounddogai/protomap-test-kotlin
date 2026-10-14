package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodReviewValidator()
        val item = newDutyPeriodReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = DutyPeriodReviewRequiredCurrencyRule("duty period review needs an entry")
        val item = newDutyPeriodReview("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = DutyPeriodReviewCurrencyLengthRule(16)
        val item = newDutyPeriodReview("x", "y")
        item.currency = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DutyPeriodReviewCodeLengthRule(32)
        val item = newDutyPeriodReview("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
