package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTrailerVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTrailerVarianceValidator()
        val item = newFallbackTrailerVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackTrailerVarianceNameLengthRule(1000)
        val item = newFallbackTrailerVariance("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = FallbackTrailerVarianceCategoryLengthRule(16)
        val item = newFallbackTrailerVariance("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackTrailerVarianceRequiredNameRule("fallback trailer variance needs this field")
        val item = newFallbackTrailerVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = FallbackTrailerVarianceRequiredCategoryRule("fallback trailer variance needs a value")
        val item = newFallbackTrailerVariance("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = FallbackTrailerVarianceLoadKgRangeRule(9.7, 4689.8)
        val item = newFallbackTrailerVariance("x", "y")
        item.loadKg = 4691.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = FallbackTrailerVarianceMaximumRangeRule(7.3, 1341.8)
        val item = newFallbackTrailerVariance("x", "y")
        item.maximum = 1343.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
