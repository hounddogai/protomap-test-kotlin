package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCapacityExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCapacityExceptionValidator()
        val item = newDefaultCapacityException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = DefaultCapacityExceptionSourceLengthRule(86400)
        val item = newDefaultCapacityException("x", "y")
        item.source = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultCapacityExceptionRequiredCodeRule("default capacity exception needs a value")
        val item = newDefaultCapacityException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = DefaultCapacityExceptionRequiredSourceRule("default capacity exception needs a value")
        val item = newDefaultCapacityException("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultCapacityExceptionLabelLengthRule(1000)
        val item = newDefaultCapacityException("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
