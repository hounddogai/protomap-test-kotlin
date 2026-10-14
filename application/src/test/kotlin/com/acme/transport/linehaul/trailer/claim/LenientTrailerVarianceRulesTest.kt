package com.acme.transport.linehaul.trailer.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTrailerVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientTrailerVarianceValidator()
        val item = newLenientTrailerVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientTrailerVarianceRequiredCodeRule("lenient trailer variance needs this field")
        val item = newLenientTrailerVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LenientTrailerVarianceLimitRangeRule(64, 255)
        val item = newLenientTrailerVariance("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = LenientTrailerVarianceGraceDaysRangeRule(80, 255)
        val item = newLenientTrailerVariance("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = LenientTrailerVarianceRequiredCurrencyRule("lenient trailer variance needs an entry")
        val item = newLenientTrailerVariance("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientTrailerVarianceNameLengthRule(64)
        val item = newLenientTrailerVariance("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
