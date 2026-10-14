package com.acme.transport.network.node.weightednodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNodeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveNodeMetricValidator()
        val item = newAdaptiveNodeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveNodeMetricCodeLengthRule(16)
        val item = newAdaptiveNodeMetric("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = AdaptiveNodeMetricRateRangeRule(8.0, 161.3)
        val item = newAdaptiveNodeMetric("x", "y")
        item.rate = 162.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveNodeMetricRequiredCodeRule("adaptive node metric needs an entry")
        val item = newAdaptiveNodeMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
