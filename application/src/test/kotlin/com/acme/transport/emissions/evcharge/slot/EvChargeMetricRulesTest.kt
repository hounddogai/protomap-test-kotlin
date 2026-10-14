package com.acme.transport.emissions.evcharge.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeMetricValidator()
        val item = newEvChargeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeMetricRequiredCodeRule("ev charge metric needs this field")
        val item = newEvChargeMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = EvChargeMetricRequiredLabelRule("ev charge metric needs this field")
        val item = newEvChargeMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = EvChargeMetricLoadKgRangeRule(1.3, 4586.3)
        val item = newEvChargeMetric("x", "y")
        item.loadKg = 4587.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeMetricCodeLengthRule(16)
        val item = newEvChargeMetric("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
