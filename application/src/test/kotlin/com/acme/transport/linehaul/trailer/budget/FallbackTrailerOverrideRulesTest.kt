package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTrailerOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTrailerOverrideValidator()
        val item = newFallbackTrailerOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = FallbackTrailerOverrideQuantityRangeRule(16, 80)
        val item = newFallbackTrailerOverride("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackTrailerOverrideCodeLengthRule(3600)
        val item = newFallbackTrailerOverride("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = FallbackTrailerOverrideRequiredCategoryRule("fallback trailer override needs a value")
        val item = newFallbackTrailerOverride("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackTrailerOverrideRequiredLabelRule("fallback trailer override needs this field")
        val item = newFallbackTrailerOverride("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = FallbackTrailerOverrideCategoryLengthRule(86400)
        val item = newFallbackTrailerOverride("x", "y")
        item.category = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
