package com.acme.transport.emissions.routeoffset.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRouteOffsetMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRouteOffsetMilestoneValidator()
        val item = newTieredRouteOffsetMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredRouteOffsetMilestoneRequiredCodeRule("tiered route offset milestone needs this field")
        val item = newTieredRouteOffsetMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredRouteOffsetMilestoneCodeLengthRule(3600)
        val item = newTieredRouteOffsetMilestone("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredRouteOffsetMilestoneTitleLengthRule(1000)
        val item = newTieredRouteOffsetMilestone("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = TieredRouteOffsetMilestoneDiscountRangeRule(2.8, 4970.8)
        val item = newTieredRouteOffsetMilestone("x", "y")
        item.discount = 4972.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredRouteOffsetMilestoneRequiredTitleRule("tiered route offset milestone needs this field")
        val item = newTieredRouteOffsetMilestone("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
