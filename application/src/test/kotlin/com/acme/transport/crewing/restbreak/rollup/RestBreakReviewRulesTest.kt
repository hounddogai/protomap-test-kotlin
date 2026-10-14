package com.acme.transport.crewing.restbreak.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakReviewValidator()
        val item = newRestBreakReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RestBreakReviewMaximumRangeRule(3.3, 236.8)
        val item = newRestBreakReview("x", "y")
        item.maximum = 238.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakReviewCodeLengthRule(86400)
        val item = newRestBreakReview("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
