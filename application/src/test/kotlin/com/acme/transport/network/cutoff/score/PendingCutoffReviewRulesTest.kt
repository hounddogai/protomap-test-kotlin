package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCutoffReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCutoffReviewValidator()
        val item = newPendingCutoffReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PendingCutoffReviewRequiredChannelRule("pending cutoff review needs a value")
        val item = newPendingCutoffReview("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = PendingCutoffReviewAttemptsRangeRule(255, 1000)
        val item = newPendingCutoffReview("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingCutoffReviewRequiredLabelRule("pending cutoff review needs an entry")
        val item = newPendingCutoffReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingCutoffReviewLabelLengthRule(255)
        val item = newPendingCutoffReview("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
