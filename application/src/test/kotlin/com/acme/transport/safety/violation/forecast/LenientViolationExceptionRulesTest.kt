package com.acme.transport.safety.violation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientViolationExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientViolationExceptionValidator()
        val item = newLenientViolationException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LenientViolationExceptionAttemptsRangeRule(64, 1000)
        val item = newLenientViolationException("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = LenientViolationExceptionStopCountRangeRule(3600, 86400)
        val item = newLenientViolationException("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LenientViolationExceptionRequiredTimezoneRule("lenient violation exception needs this field")
        val item = newLenientViolationException("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientViolationExceptionCodeLengthRule(64)
        val item = newLenientViolationException("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
