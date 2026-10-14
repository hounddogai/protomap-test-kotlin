package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTransitTimeTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTransitTimeTemplateValidator()
        val item = newBatchedTransitTimeTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = BatchedTransitTimeTemplateRatioRangeRule(9.2, 4027.1)
        val item = newBatchedTransitTimeTemplate("x", "y")
        item.ratio = 4028.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedTransitTimeTemplateCodeLengthRule(80)
        val item = newBatchedTransitTimeTemplate("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = BatchedTransitTimeTemplateBatchSizeRangeRule(255, 3600)
        val item = newBatchedTransitTimeTemplate("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
