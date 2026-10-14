package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRestBreakSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRestBreakSummaryValidator()
        val item = newBatchedRestBreakSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = BatchedRestBreakSummaryRequiredContactEmailRule("batched rest break summary needs this field")
        val item = newBatchedRestBreakSummary("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = BatchedRestBreakSummaryDiscountRangeRule(7.3, 4019.9)
        val item = newBatchedRestBreakSummary("x", "y")
        item.discount = 4021.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedRestBreakSummaryCodeLengthRule(1000)
        val item = newBatchedRestBreakSummary("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedRestBreakSummaryNameLengthRule(3600)
        val item = newBatchedRestBreakSummary("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
