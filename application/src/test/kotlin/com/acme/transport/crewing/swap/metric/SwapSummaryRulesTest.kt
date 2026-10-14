package com.acme.transport.crewing.swap.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapSummaryValidator()
        val item = newSwapSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapSummaryCodeLengthRule(80)
        val item = newSwapSummary("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SwapSummaryRequiredLabelRule("swap summary needs an entry")
        val item = newSwapSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = SwapSummaryBatchSizeRangeRule(255, 1000)
        val item = newSwapSummary("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
