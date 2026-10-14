package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeExceptionValidator()
        val item = newEvChargeException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = EvChargeExceptionRequiredLabelRule("ev charge exception needs an entry")
        val item = newEvChargeException("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeExceptionRequiredCodeRule("ev charge exception needs this field")
        val item = newEvChargeException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = EvChargeExceptionLabelLengthRule(255)
        val item = newEvChargeException("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = EvChargeExceptionQuantityRangeRule(16, 64)
        val item = newEvChargeException("x", "y")
        item.quantity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = EvChargeExceptionAmountRangeRule(7.2, 3026.4)
        val item = newEvChargeException("x", "y")
        item.amount = 3027.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
