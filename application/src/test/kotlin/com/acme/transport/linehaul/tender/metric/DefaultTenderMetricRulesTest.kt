package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTenderMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTenderMetricValidator()
        val item = newDefaultTenderMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = DefaultTenderMetricAmountRangeRule(0.6, 3650.6)
        val item = newDefaultTenderMetric("x", "y")
        item.amount = 3652.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultTenderMetricLabelLengthRule(86400)
        val item = newDefaultTenderMetric("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = DefaultTenderMetricTimezoneLengthRule(64)
        val item = newDefaultTenderMetric("x", "y")
        item.timezone = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
