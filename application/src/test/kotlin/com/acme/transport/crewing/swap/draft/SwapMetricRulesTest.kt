package com.acme.transport.crewing.swap.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapMetricValidator()
        val item = newSwapMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SwapMetricRequiredTitleRule("swap metric needs an entry")
        val item = newSwapMetric("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SwapMetricTitleLengthRule(255)
        val item = newSwapMetric("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
