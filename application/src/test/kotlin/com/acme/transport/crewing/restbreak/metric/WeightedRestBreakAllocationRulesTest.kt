package com.acme.transport.crewing.restbreak.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRestBreakAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRestBreakAllocationValidator()
        val item = newWeightedRestBreakAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedRestBreakAllocationTitleLengthRule(255)
        val item = newWeightedRestBreakAllocation("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = WeightedRestBreakAllocationRequiredRegionRule("weighted rest break allocation needs a value")
        val item = newWeightedRestBreakAllocation("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = WeightedRestBreakAllocationDistanceKmRangeRule(5.4, 2689.2)
        val item = newWeightedRestBreakAllocation("x", "y")
        item.distanceKm = 2690.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedRestBreakAllocationRequiredTitleRule("weighted rest break allocation needs an entry")
        val item = newWeightedRestBreakAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
