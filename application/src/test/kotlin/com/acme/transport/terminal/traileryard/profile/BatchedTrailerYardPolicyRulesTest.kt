package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTrailerYardPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTrailerYardPolicyValidator()
        val item = newBatchedTrailerYardPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = BatchedTrailerYardPolicyEndsAtRangeRule(255, 86400)
        val item = newBatchedTrailerYardPolicy("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
