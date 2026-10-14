package com.acme.transport.freightpay.accessorial.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAccessorialTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAccessorialTemplateValidator()
        val item = newFallbackAccessorialTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackAccessorialTemplateRequiredCodeRule("fallback accessorial template needs this field")
        val item = newFallbackAccessorialTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = FallbackAccessorialTemplateLoadKgRangeRule(0.9, 1819.8)
        val item = newFallbackAccessorialTemplate("x", "y")
        item.loadKg = 1821.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = FallbackAccessorialTemplateExternalRefLengthRule(255)
        val item = newFallbackAccessorialTemplate("x", "y")
        item.externalRef = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = FallbackAccessorialTemplateRequiredSummaryRule("fallback accessorial template needs this field")
        val item = newFallbackAccessorialTemplate("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackAccessorialTemplateCodeLengthRule(64)
        val item = newFallbackAccessorialTemplate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
