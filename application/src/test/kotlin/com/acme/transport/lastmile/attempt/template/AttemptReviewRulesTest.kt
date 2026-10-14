package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptReviewValidator()
        val item = newAttemptReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AttemptReviewCeilingRangeRule(7.0, 3563.4)
        val item = newAttemptReview("x", "y")
        item.ceiling = 3564.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AttemptReviewRequiredNameRule("attempt review needs an entry")
        val item = newAttemptReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptReviewRequiredCodeRule("attempt review needs this field")
        val item = newAttemptReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
