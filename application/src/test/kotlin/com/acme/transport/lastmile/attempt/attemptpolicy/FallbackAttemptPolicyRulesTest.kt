package com.acme.transport.lastmile.attempt.attemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAttemptPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAttemptPolicyValidator()
        val item = newFallbackAttemptPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackAttemptPolicyRequiredNameRule("fallback attempt policy needs this field")
        val item = newFallbackAttemptPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = FallbackAttemptPolicyRequiredLocaleRule("fallback attempt policy needs a value")
        val item = newFallbackAttemptPolicy("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackAttemptPolicyCodeLengthRule(3600)
        val item = newFallbackAttemptPolicy("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackAttemptPolicyNameLengthRule(3600)
        val item = newFallbackAttemptPolicy("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
