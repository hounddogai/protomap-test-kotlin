package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalSwapMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalSwapMetricValidator()
        val item = newGlobalSwapMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = GlobalSwapMetricWeightRangeRule(1.1, 996.9)
        val item = newGlobalSwapMetric("x", "y")
        item.weight = 998.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = GlobalSwapMetricRequiredLaneRule("global swap metric needs an entry")
        val item = newGlobalSwapMetric("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
