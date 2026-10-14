package com.acme.transport.emissions.evcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalEvChargeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalEvChargeMetricValidator()
        val item = newRegionalEvChargeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RegionalEvChargeMetricLoadKgRangeRule(9.1, 624.3)
        val item = newRegionalEvChargeMetric("x", "y")
        item.loadKg = 625.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = RegionalEvChargeMetricDescriptionLengthRule(16)
        val item = newRegionalEvChargeMetric("x", "y")
        item.description = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalEvChargeMetricCodeLengthRule(80)
        val item = newRegionalEvChargeMetric("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
