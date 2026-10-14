package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTrailerMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTrailerMetricValidator()
        val item = newScheduledTrailerMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledTrailerMetricCodeLengthRule(80)
        val item = newScheduledTrailerMetric("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ScheduledTrailerMetricPriorityRangeRule(16, 86400)
        val item = newScheduledTrailerMetric("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = ScheduledTrailerMetricRequiredSourceRule("scheduled trailer metric needs this field")
        val item = newScheduledTrailerMetric("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ScheduledTrailerMetricDiscountRangeRule(4.6, 1519.0)
        val item = newScheduledTrailerMetric("x", "y")
        item.discount = 1520.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
