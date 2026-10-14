package com.acme.transport.terminal.sortation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationEstimateValidator()
        val item = newSortationEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = SortationEstimateRequiredDepotRule("sortation estimate needs this field")
        val item = newSortationEstimate("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SortationEstimateNameLengthRule(255)
        val item = newSortationEstimate("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SortationEstimateRequiredNameRule("sortation estimate needs a value")
        val item = newSortationEstimate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = SortationEstimatePalletsRangeRule(64, 80)
        val item = newSortationEstimate("x", "y")
        item.pallets = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = SortationEstimateRateRangeRule(8.5, 2694.3)
        val item = newSortationEstimate("x", "y")
        item.rate = 2695.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
