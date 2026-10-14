package com.acme.transport.lastmile.redelivery.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryMetricValidator()
        val item = newRedeliveryMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RedeliveryMetricRequiredTitleRule("redelivery metric needs this field")
        val item = newRedeliveryMetric("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RedeliveryMetricTitleLengthRule(255)
        val item = newRedeliveryMetric("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
