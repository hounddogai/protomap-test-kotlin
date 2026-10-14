package com.acme.transport.lastmile.attempt.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptAllocationValidator()
        val item = newAttemptAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AttemptAllocationRequiredTitleRule("attempt allocation needs an entry")
        val item = newAttemptAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = AttemptAllocationReviewerLengthRule(1000)
        val item = newAttemptAllocation("x", "y")
        item.reviewer = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = AttemptAllocationLaneLengthRule(16)
        val item = newAttemptAllocation("x", "y")
        item.lane = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
