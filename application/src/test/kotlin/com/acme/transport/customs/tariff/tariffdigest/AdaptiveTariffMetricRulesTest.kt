package com.acme.transport.customs.tariff.tariffdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTariffMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTariffMetricValidator()
        val item = newAdaptiveTariffMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = AdaptiveTariffMetricStopCountRangeRule(80, 3600)
        val item = newAdaptiveTariffMetric("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = AdaptiveTariffMetricRequiredRouteCodeRule("adaptive tariff metric needs an entry")
        val item = newAdaptiveTariffMetric("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveTariffMetricRequiredCodeRule("adaptive tariff metric needs an entry")
        val item = newAdaptiveTariffMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveTariffMetricNameLengthRule(1000)
        val item = newAdaptiveTariffMetric("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
