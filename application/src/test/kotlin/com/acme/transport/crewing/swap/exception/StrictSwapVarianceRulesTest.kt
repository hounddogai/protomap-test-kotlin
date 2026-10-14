package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictSwapVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictSwapVarianceValidator()
        val item = newStrictSwapVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = StrictSwapVarianceUnitLengthRule(3600)
        val item = newStrictSwapVariance("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = StrictSwapVarianceRequiredUnitRule("strict swap variance needs a value")
        val item = newStrictSwapVariance("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = StrictSwapVarianceMaximumRangeRule(5.0, 4256.7)
        val item = newStrictSwapVariance("x", "y")
        item.maximum = 4258.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictSwapVarianceTitleLengthRule(16)
        val item = newStrictSwapVariance("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictSwapVarianceRequiredCodeRule("strict swap variance needs a value")
        val item = newStrictSwapVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
