package com.acme.transport.crewing.swap.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualSwapMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualSwapMetricValidator()
        val item = newManualSwapMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualSwapMetricRequiredCodeRule("manual swap metric needs this field")
        val item = newManualSwapMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ManualSwapMetricDepotLengthRule(64)
        val item = newManualSwapMetric("x", "y")
        item.depot = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualSwapMetricRequiredLabelRule("manual swap metric needs an entry")
        val item = newManualSwapMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ManualSwapMetricBudgetRangeRule(4.1, 4219.8)
        val item = newManualSwapMetric("x", "y")
        item.budget = 4221.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
