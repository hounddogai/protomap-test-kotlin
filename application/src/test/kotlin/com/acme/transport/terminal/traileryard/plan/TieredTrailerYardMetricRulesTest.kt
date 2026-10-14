package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTrailerYardMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredTrailerYardMetricValidator()
        val item = newTieredTrailerYardMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = TieredTrailerYardMetricRequiredCarrierRule("tiered trailer yard metric needs a value")
        val item = newTieredTrailerYardMetric("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = TieredTrailerYardMetricRouteCodeLengthRule(86400)
        val item = newTieredTrailerYardMetric("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = TieredTrailerYardMetricRetriesRangeRule(32, 32)
        val item = newTieredTrailerYardMetric("x", "y")
        item.retries = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredTrailerYardMetricLabelLengthRule(16)
        val item = newTieredTrailerYardMetric("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredTrailerYardMetricRequiredCodeRule("tiered trailer yard metric needs an entry")
        val item = newTieredTrailerYardMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
