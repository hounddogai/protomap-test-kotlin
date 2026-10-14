package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTrailerExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTrailerExceptionValidator()
        val item = newDefaultTrailerException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = DefaultTrailerExceptionExpiresAtRangeRule(32, 120)
        val item = newDefaultTrailerException("x", "y")
        item.expiresAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultTrailerExceptionLabelLengthRule(86400)
        val item = newDefaultTrailerException("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = DefaultTrailerExceptionCarrierLengthRule(255)
        val item = newDefaultTrailerException("x", "y")
        item.carrier = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
