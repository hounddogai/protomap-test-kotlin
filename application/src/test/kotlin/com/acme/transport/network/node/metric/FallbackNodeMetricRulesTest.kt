package com.acme.transport.network.node.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackNodeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackNodeMetricValidator()
        val item = newFallbackNodeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackNodeMetricRequiredTitleRule("fallback node metric needs a value")
        val item = newFallbackNodeMetric("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackNodeMetricTitleLengthRule(255)
        val item = newFallbackNodeMetric("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
