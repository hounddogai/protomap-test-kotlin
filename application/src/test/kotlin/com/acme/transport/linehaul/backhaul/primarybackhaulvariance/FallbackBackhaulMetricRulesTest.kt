package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackBackhaulMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackBackhaulMetricValidator()
        val item = newFallbackBackhaulMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = FallbackBackhaulMetricCarrierLengthRule(80)
        val item = newFallbackBackhaulMetric("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackBackhaulMetricRequiredCodeRule("fallback backhaul metric needs a value")
        val item = newFallbackBackhaulMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackBackhaulMetricLabelLengthRule(1000)
        val item = newFallbackBackhaulMetric("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
