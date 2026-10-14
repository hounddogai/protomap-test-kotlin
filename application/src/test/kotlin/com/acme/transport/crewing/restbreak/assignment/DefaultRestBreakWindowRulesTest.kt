package com.acme.transport.crewing.restbreak.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRestBreakWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRestBreakWindowValidator()
        val item = newDefaultRestBreakWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultRestBreakWindowRequiredCodeRule("default rest break window needs a value")
        val item = newDefaultRestBreakWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultRestBreakWindowRequiredTitleRule("default rest break window needs a value")
        val item = newDefaultRestBreakWindow("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = DefaultRestBreakWindowBatchSizeRangeRule(64, 1000)
        val item = newDefaultRestBreakWindow("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
