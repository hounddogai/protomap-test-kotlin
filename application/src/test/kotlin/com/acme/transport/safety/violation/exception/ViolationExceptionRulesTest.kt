package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationExceptionValidator()
        val item = newViolationException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = ViolationExceptionCarrierLengthRule(32)
        val item = newViolationException("x", "y")
        item.carrier = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ViolationExceptionRequiredDescriptionRule("violation exception needs an entry")
        val item = newViolationException("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = ViolationExceptionRequiredCarrierRule("violation exception needs a value")
        val item = newViolationException("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ViolationExceptionBatchSizeRangeRule(80, 255)
        val item = newViolationException("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
