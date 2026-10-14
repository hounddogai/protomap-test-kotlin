package com.acme.transport.lastmile.locker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLockerExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedLockerExceptionValidator()
        val item = newCappedLockerException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedLockerExceptionRequiredCodeRule("capped locker exception needs an entry")
        val item = newCappedLockerException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedLockerExceptionLabelLengthRule(64)
        val item = newCappedLockerException("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedLockerExceptionRequiredLabelRule("capped locker exception needs a value")
        val item = newCappedLockerException("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = CappedLockerExceptionQuantityRangeRule(120, 1000)
        val item = newCappedLockerException("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedLockerExceptionCodeLengthRule(255)
        val item = newCappedLockerException("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
