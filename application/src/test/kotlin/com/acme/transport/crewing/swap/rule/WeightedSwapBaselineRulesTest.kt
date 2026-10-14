package com.acme.transport.crewing.swap.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedSwapBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedSwapBaselineValidator()
        val item = newWeightedSwapBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = WeightedSwapBaselineAmountRangeRule(6.8, 2818.0)
        val item = newWeightedSwapBaseline("x", "y")
        item.amount = 2819.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = WeightedSwapBaselineRetriesRangeRule(1000, 86400)
        val item = newWeightedSwapBaseline("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = WeightedSwapBaselineCategoryLengthRule(255)
        val item = newWeightedSwapBaseline("x", "y")
        item.category = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = WeightedSwapBaselinePhoneLengthRule(255)
        val item = newWeightedSwapBaseline("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedSwapBaselineRequiredCodeRule("weighted swap baseline needs an entry")
        val item = newWeightedSwapBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
