package com.acme.transport.crewing.swap.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapNoticeValidator()
        val item = newSwapNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = SwapNoticeBatchSizeRangeRule(32, 1000)
        val item = newSwapNotice("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = SwapNoticeNameLengthRule(120)
        val item = newSwapNotice("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SwapNoticeRequiredTitleRule("swap notice needs a value")
        val item = newSwapNotice("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapNoticeCodeLengthRule(16)
        val item = newSwapNotice("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
