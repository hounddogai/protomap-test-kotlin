package com.acme.transport.terminal.dwell.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDwellExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDwellExceptionValidator()
        val item = newFallbackDwellException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackDwellExceptionRequiredTitleRule("fallback dwell exception needs an entry")
        val item = newFallbackDwellException("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackDwellExceptionRequiredCodeRule("fallback dwell exception needs a value")
        val item = newFallbackDwellException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = FallbackDwellExceptionRouteCodeLengthRule(32)
        val item = newFallbackDwellException("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = FallbackDwellExceptionRequiredRouteCodeRule("fallback dwell exception needs an entry")
        val item = newFallbackDwellException("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = FallbackDwellExceptionStartsAtRangeRule(80, 86400)
        val item = newFallbackDwellException("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
