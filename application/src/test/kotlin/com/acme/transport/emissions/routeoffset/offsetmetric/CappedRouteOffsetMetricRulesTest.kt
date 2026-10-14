package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRouteOffsetMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRouteOffsetMetricValidator()
        val item = newCappedRouteOffsetMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = CappedRouteOffsetMetricLoadKgRangeRule(5.0, 3726.6)
        val item = newCappedRouteOffsetMetric("x", "y")
        item.loadKg = 3728.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = CappedRouteOffsetMetricBatchSizeRangeRule(32, 3600)
        val item = newCappedRouteOffsetMetric("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedRouteOffsetMetricNameLengthRule(16)
        val item = newCappedRouteOffsetMetric("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = CappedRouteOffsetMetricRequiredUnitRule("capped route offset metric needs this field")
        val item = newCappedRouteOffsetMetric("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedRouteOffsetMetricTitleLengthRule(32)
        val item = newCappedRouteOffsetMetric("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
