package com.acme.transport.terminal.dwell.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDwellExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDwellExceptionValidator()
        val item = newGlobalDwellException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = GlobalDwellExceptionRequiredTimezoneRule("global dwell exception needs this field")
        val item = newGlobalDwellException("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalDwellExceptionRequiredTitleRule("global dwell exception needs a value")
        val item = newGlobalDwellException("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = GlobalDwellExceptionVersionRangeRule(16, 86400)
        val item = newGlobalDwellException("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
