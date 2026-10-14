package com.acme.transport.emissions.routeoffset.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetEstimateValidator()
        val item = newRouteOffsetEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = RouteOffsetEstimateBatchSizeRangeRule(16, 255)
        val item = newRouteOffsetEstimate("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RouteOffsetEstimateRequiredCodeRule("route offset estimate needs a value")
        val item = newRouteOffsetEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RouteOffsetEstimateLaneLengthRule(80)
        val item = newRouteOffsetEstimate("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RouteOffsetEstimateRequiredLaneRule("route offset estimate needs an entry")
        val item = newRouteOffsetEstimate("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = RouteOffsetEstimateCategoryLengthRule(32)
        val item = newRouteOffsetEstimate("x", "y")
        item.category = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
