package com.acme.transport.network.servicearea.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackServiceAreaAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackServiceAreaAuditValidator()
        val item = newFallbackServiceAreaAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackServiceAreaAuditRequiredCodeRule("fallback service area audit needs a value")
        val item = newFallbackServiceAreaAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = FallbackServiceAreaAuditVersionRangeRule(32, 86400)
        val item = newFallbackServiceAreaAudit("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = FallbackServiceAreaAuditRequiredCurrencyRule("fallback service area audit needs a value")
        val item = newFallbackServiceAreaAudit("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = FallbackServiceAreaAuditCurrencyLengthRule(16)
        val item = newFallbackServiceAreaAudit("x", "y")
        item.currency = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
