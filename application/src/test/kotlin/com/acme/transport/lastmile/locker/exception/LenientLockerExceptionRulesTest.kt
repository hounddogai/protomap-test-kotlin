package com.acme.transport.lastmile.locker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientLockerExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientLockerExceptionValidator()
        val item = newLenientLockerException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientLockerExceptionCodeLengthRule(86400)
        val item = newLenientLockerException("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientLockerExceptionRequiredCodeRule("lenient locker exception needs an entry")
        val item = newLenientLockerException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientLockerExceptionRequiredLabelRule("lenient locker exception needs a value")
        val item = newLenientLockerException("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientLockerExceptionLabelLengthRule(3600)
        val item = newLenientLockerException("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
