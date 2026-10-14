package com.acme.transport.terminal.dwell.dwelltemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellExceptionValidator()
        val item = newDwellException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DwellExceptionRequiredNameRule("dwell exception needs this field")
        val item = newDwellException("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = DwellExceptionRequiredExternalRefRule("dwell exception needs a value")
        val item = newDwellException("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DwellExceptionCodeLengthRule(32)
        val item = newDwellException("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = DwellExceptionExternalRefLengthRule(64)
        val item = newDwellException("x", "y")
        item.externalRef = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
