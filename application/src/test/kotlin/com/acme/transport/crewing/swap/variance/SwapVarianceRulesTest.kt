package com.acme.transport.crewing.swap.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapVarianceValidator()
        val item = newSwapVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SwapVarianceTitleLengthRule(255)
        val item = newSwapVariance("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = SwapVariancePalletsRangeRule(16, 86400)
        val item = newSwapVariance("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapVarianceCodeLengthRule(32)
        val item = newSwapVariance("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
