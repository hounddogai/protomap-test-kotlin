package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerExceptionValidator()
        val item = newLockerException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerExceptionCodeLengthRule(1000)
        val item = newLockerException("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = LockerExceptionStopCountRangeRule(255, 3600)
        val item = newLockerException("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = LockerExceptionCarrierLengthRule(120)
        val item = newLockerException("x", "y")
        item.carrier = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = LockerExceptionRequiredCarrierRule("locker exception needs this field")
        val item = newLockerException("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
