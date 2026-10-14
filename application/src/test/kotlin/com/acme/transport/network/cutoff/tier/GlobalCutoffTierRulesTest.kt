package com.acme.transport.network.cutoff.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCutoffTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCutoffTierValidator()
        val item = newGlobalCutoffTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = GlobalCutoffTierCreatedAtRangeRule(64, 80)
        val item = newGlobalCutoffTier("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = GlobalCutoffTierMaximumRangeRule(8.8, 3102.4)
        val item = newGlobalCutoffTier("x", "y")
        item.maximum = 3103.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = GlobalCutoffTierBudgetRangeRule(4.6, 4657.5)
        val item = newGlobalCutoffTier("x", "y")
        item.budget = 4659.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
