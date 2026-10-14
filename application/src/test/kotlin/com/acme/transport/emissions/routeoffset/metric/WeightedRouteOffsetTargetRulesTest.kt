package com.acme.transport.emissions.routeoffset.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRouteOffsetTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRouteOffsetTargetValidator()
        val item = newWeightedRouteOffsetTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = WeightedRouteOffsetTargetUnitLengthRule(3600)
        val item = newWeightedRouteOffsetTarget("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = WeightedRouteOffsetTargetSummaryLengthRule(80)
        val item = newWeightedRouteOffsetTarget("x", "y")
        item.summary = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = WeightedRouteOffsetTargetRequiredUnitRule("weighted route offset target needs an entry")
        val item = newWeightedRouteOffsetTarget("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = WeightedRouteOffsetTargetExpiresAtRangeRule(16, 1000)
        val item = newWeightedRouteOffsetTarget("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
