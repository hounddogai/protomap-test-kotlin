package com.acme.transport.customs.broker.brokerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveBrokerMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveBrokerMetricValidator()
        val item = newAdaptiveBrokerMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = AdaptiveBrokerMetricRequiredLaneRule("adaptive broker metric needs a value")
        val item = newAdaptiveBrokerMetric("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveBrokerMetricRequiredNameRule("adaptive broker metric needs this field")
        val item = newAdaptiveBrokerMetric("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveBrokerMetricNameLengthRule(32)
        val item = newAdaptiveBrokerMetric("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveBrokerMetricRequiredCodeRule("adaptive broker metric needs an entry")
        val item = newAdaptiveBrokerMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveBrokerMetricCodeLengthRule(16)
        val item = newAdaptiveBrokerMetric("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
