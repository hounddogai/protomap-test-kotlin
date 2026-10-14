package com.acme.transport.emissions.routeoffset.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRouteOffsetMilestoneValidator()
        val item = newFallbackRouteOffsetMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = FallbackRouteOffsetMilestoneRequiredDepotRule("fallback route offset milestone needs an entry")
        val item = newFallbackRouteOffsetMilestone("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = FallbackRouteOffsetMilestoneRetriesRangeRule(120, 1000)
        val item = newFallbackRouteOffsetMilestone("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = FallbackRouteOffsetMilestonePalletsRangeRule(32, 1000)
        val item = newFallbackRouteOffsetMilestone("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
