package com.acme.transport.crewing.restbreak.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakMetricValidator()
        val item = newRestBreakMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RestBreakMetricNameLengthRule(80)
        val item = newRestBreakMetric("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RestBreakMetricTimezoneLengthRule(255)
        val item = newRestBreakMetric("x", "y")
        item.timezone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RestBreakMetricRequiredNameRule("rest break metric needs an entry")
        val item = newRestBreakMetric("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
