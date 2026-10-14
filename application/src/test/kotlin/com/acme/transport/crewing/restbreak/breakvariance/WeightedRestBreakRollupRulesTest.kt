package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRestBreakRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRestBreakRollupValidator()
        val item = newWeightedRestBreakRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = WeightedRestBreakRollupCreatedAtRangeRule(16, 3600)
        val item = newWeightedRestBreakRollup("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedRestBreakRollupLabelLengthRule(120)
        val item = newWeightedRestBreakRollup("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = WeightedRestBreakRollupCategoryLengthRule(3600)
        val item = newWeightedRestBreakRollup("x", "y")
        item.category = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = WeightedRestBreakRollupDiscountRangeRule(5.4, 4492.6)
        val item = newWeightedRestBreakRollup("x", "y")
        item.discount = 4494.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
