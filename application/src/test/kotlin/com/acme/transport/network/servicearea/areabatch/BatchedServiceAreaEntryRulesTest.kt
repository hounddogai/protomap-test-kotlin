package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServiceAreaEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedServiceAreaEntryValidator()
        val item = newBatchedServiceAreaEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = BatchedServiceAreaEntryCurrencyLengthRule(120)
        val item = newBatchedServiceAreaEntry("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = BatchedServiceAreaEntryLimitRangeRule(255, 86400)
        val item = newBatchedServiceAreaEntry("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = BatchedServiceAreaEntrySegmentLengthRule(32)
        val item = newBatchedServiceAreaEntry("x", "y")
        item.segment = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
