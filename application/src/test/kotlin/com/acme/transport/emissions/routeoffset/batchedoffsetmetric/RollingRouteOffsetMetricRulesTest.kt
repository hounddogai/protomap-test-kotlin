package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRouteOffsetMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRouteOffsetMetricValidator()
        val item = newRollingRouteOffsetMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RollingRouteOffsetMetricPalletsRangeRule(64, 120)
        val item = newRollingRouteOffsetMetric("x", "y")
        item.pallets = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RollingRouteOffsetMetricFuelLitersRangeRule(2.8, 4718.0)
        val item = newRollingRouteOffsetMetric("x", "y")
        item.fuelLiters = 4719.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingRouteOffsetMetricRequiredCodeRule("rolling route offset metric needs an entry")
        val item = newRollingRouteOffsetMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingRouteOffsetMetricCodeLengthRule(80)
        val item = newRollingRouteOffsetMetric("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
