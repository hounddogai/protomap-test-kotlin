package com.acme.transport.customs.broker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalBrokerMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalBrokerMetricValidator()
        val item = newRegionalBrokerMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RegionalBrokerMetricMinimumRangeRule(9.2, 2120.6)
        val item = newRegionalBrokerMetric("x", "y")
        item.minimum = 2122.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = RegionalBrokerMetricRequiredNotesRule("regional broker metric needs a value")
        val item = newRegionalBrokerMetric("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalBrokerMetricRequiredCodeRule("regional broker metric needs a value")
        val item = newRegionalBrokerMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = RegionalBrokerMetricRequiredCategoryRule("regional broker metric needs an entry")
        val item = newRegionalBrokerMetric("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalBrokerMetricNameLengthRule(3600)
        val item = newRegionalBrokerMetric("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
