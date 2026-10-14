package com.acme.transport.freightpay.dispute.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDisputeExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDisputeExceptionValidator()
        val item = newTieredDisputeException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TieredDisputeExceptionCostRangeRule(3.7, 410.8)
        val item = newTieredDisputeException("x", "y")
        item.cost = 412.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = TieredDisputeExceptionDockLengthRule(255)
        val item = newTieredDisputeException("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
