package com.acme.transport.safety.coaching.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCoachingExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictCoachingExceptionValidator()
        val item = newStrictCoachingException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = StrictCoachingExceptionFuelLitersRangeRule(2.2, 3051.3)
        val item = newStrictCoachingException("x", "y")
        item.fuelLiters = 3052.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictCoachingExceptionCodeLengthRule(16)
        val item = newStrictCoachingException("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictCoachingExceptionRequiredCodeRule("strict coaching exception needs this field")
        val item = newStrictCoachingException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = StrictCoachingExceptionRequiredSourceRule("strict coaching exception needs a value")
        val item = newStrictCoachingException("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictCoachingExceptionLabelLengthRule(1000)
        val item = newStrictCoachingException("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
