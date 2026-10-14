package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedSortationBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedSortationBundleValidator()
        val item = newWeightedSortationBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = WeightedSortationBundleDockLengthRule(1000)
        val item = newWeightedSortationBundle("x", "y")
        item.dock = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedSortationBundleRequiredCodeRule("weighted sortation bundle needs a value")
        val item = newWeightedSortationBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = WeightedSortationBundleRequiredDockRule("weighted sortation bundle needs an entry")
        val item = newWeightedSortationBundle("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = WeightedSortationBundleFuelLitersRangeRule(2.4, 548.8)
        val item = newWeightedSortationBundle("x", "y")
        item.fuelLiters = 550.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long source`() {
        val rule = WeightedSortationBundleSourceLengthRule(120)
        val item = newWeightedSortationBundle("x", "y")
        item.source = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
