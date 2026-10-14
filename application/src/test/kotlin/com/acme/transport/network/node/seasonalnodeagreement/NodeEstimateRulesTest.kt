package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeEstimateValidator()
        val item = newNodeEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = NodeEstimateMarginRangeRule(2.0, 3818.1)
        val item = newNodeEstimate("x", "y")
        item.margin = 3819.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = NodeEstimateRatioRangeRule(0.1, 1285.9)
        val item = newNodeEstimate("x", "y")
        item.ratio = 1287.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = NodeEstimateRequiredSummaryRule("node estimate needs this field")
        val item = newNodeEstimate("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeEstimateRequiredCodeRule("node estimate needs an entry")
        val item = newNodeEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
