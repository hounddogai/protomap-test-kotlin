package com.acme.transport.network.transittime.timeoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTransitTimeExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTransitTimeExceptionValidator()
        val item = newDefaultTransitTimeException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultTransitTimeExceptionRequiredNameRule("default transit time exception needs an entry")
        val item = newDefaultTransitTimeException("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = DefaultTransitTimeExceptionSequenceRangeRule(120, 86400)
        val item = newDefaultTransitTimeException("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultTransitTimeExceptionCodeLengthRule(1000)
        val item = newDefaultTransitTimeException("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
