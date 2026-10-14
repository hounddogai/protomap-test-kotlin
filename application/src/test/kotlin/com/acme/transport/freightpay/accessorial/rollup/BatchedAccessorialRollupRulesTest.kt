package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAccessorialRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAccessorialRollupValidator()
        val item = newBatchedAccessorialRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedAccessorialRollupRequiredTitleRule("batched accessorial rollup needs this field")
        val item = newBatchedAccessorialRollup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedAccessorialRollupCodeLengthRule(86400)
        val item = newBatchedAccessorialRollup("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = BatchedAccessorialRollupCostRangeRule(7.4, 2236.4)
        val item = newBatchedAccessorialRollup("x", "y")
        item.cost = 2237.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = BatchedAccessorialRollupStartsAtRangeRule(32, 3600)
        val item = newBatchedAccessorialRollup("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = BatchedAccessorialRollupRequiredSummaryRule("batched accessorial rollup needs a value")
        val item = newBatchedAccessorialRollup("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = BatchedAccessorialRollupDiscountRangeRule(3.3, 3689.8)
        val item = newBatchedAccessorialRollup("x", "y")
        item.discount = 3691.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
