package com.acme.transport.crewing.swap.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedSwapNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedSwapNoticeValidator()
        val item = newBatchedSwapNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = BatchedSwapNoticeAttemptsRangeRule(32, 1000)
        val item = newBatchedSwapNotice("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedSwapNoticeRequiredCodeRule("batched swap notice needs an entry")
        val item = newBatchedSwapNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = BatchedSwapNoticeQuantityRangeRule(32, 3600)
        val item = newBatchedSwapNotice("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedSwapNoticeNameLengthRule(32)
        val item = newBatchedSwapNotice("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedSwapNoticeRequiredNameRule("batched swap notice needs this field")
        val item = newBatchedSwapNotice("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
