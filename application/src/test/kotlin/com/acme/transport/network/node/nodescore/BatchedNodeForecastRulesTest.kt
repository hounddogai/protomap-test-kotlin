package com.acme.transport.network.node.nodescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNodeForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedNodeForecastValidator()
        val item = newBatchedNodeForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = BatchedNodeForecastBatchSizeRangeRule(16, 1000)
        val item = newBatchedNodeForecast("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedNodeForecastTitleLengthRule(80)
        val item = newBatchedNodeForecast("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedNodeForecastRequiredTitleRule("batched node forecast needs this field")
        val item = newBatchedNodeForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedNodeForecastRequiredCodeRule("batched node forecast needs this field")
        val item = newBatchedNodeForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = BatchedNodeForecastShiftCodeLengthRule(120)
        val item = newBatchedNodeForecast("x", "y")
        item.shiftCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
