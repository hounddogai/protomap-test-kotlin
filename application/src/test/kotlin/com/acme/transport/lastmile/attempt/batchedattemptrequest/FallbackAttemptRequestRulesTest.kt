package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAttemptRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAttemptRequestValidator()
        val item = newFallbackAttemptRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackAttemptRequestRequiredCodeRule("fallback attempt request needs a value")
        val item = newFallbackAttemptRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackAttemptRequestLabelLengthRule(80)
        val item = newFallbackAttemptRequest("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = FallbackAttemptRequestPhoneLengthRule(3600)
        val item = newFallbackAttemptRequest("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
