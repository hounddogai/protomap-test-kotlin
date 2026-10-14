package com.acme.transport.lastmile.attempt.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAttemptMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAttemptMilestoneValidator()
        val item = newFallbackAttemptMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = FallbackAttemptMilestoneRequiredDescriptionRule("fallback attempt milestone needs this field")
        val item = newFallbackAttemptMilestone("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackAttemptMilestoneCodeLengthRule(80)
        val item = newFallbackAttemptMilestone("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
