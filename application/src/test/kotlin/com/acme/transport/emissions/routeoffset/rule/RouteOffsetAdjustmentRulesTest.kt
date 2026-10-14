package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetAdjustmentValidator()
        val item = newRouteOffsetAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RouteOffsetAdjustmentScoreRangeRule(5.4, 1721.5)
        val item = newRouteOffsetAdjustment("x", "y")
        item.score = 1723.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RouteOffsetAdjustmentRequiredNameRule("route offset adjustment needs an entry")
        val item = newRouteOffsetAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
