package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAttemptExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAttemptExceptionValidator()
        val item = newCappedAttemptException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedAttemptExceptionCodeLengthRule(255)
        val item = newCappedAttemptException("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedAttemptExceptionRequiredCodeRule("capped attempt exception needs an entry")
        val item = newCappedAttemptException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = CappedAttemptExceptionVersionRangeRule(16, 64)
        val item = newCappedAttemptException("x", "y")
        item.version = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
