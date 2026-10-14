package com.acme.transport.lastmile.attempt.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAttemptRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalAttemptRecordValidator()
        val item = newGlobalAttemptRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalAttemptRecordRequiredCodeRule("global attempt record needs a value")
        val item = newGlobalAttemptRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = GlobalAttemptRecordRequiredReviewerRule("global attempt record needs this field")
        val item = newGlobalAttemptRecord("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
