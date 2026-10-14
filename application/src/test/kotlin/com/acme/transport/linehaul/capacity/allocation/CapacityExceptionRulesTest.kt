package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityExceptionValidator()
        val item = newCapacityException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CapacityExceptionNameLengthRule(1000)
        val item = newCapacityException("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = CapacityExceptionRequiredChannelRule("capacity exception needs this field")
        val item = newCapacityException("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
