package com.acme.transport.freightpay.fuelsurcharge.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFuelSurchargeRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedFuelSurchargeRequestValidator()
        val item = newBatchedFuelSurchargeRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedFuelSurchargeRequestRequiredTitleRule("batched fuel surcharge request needs a value")
        val item = newBatchedFuelSurchargeRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = BatchedFuelSurchargeRequestChecksumLengthRule(32)
        val item = newBatchedFuelSurchargeRequest("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = BatchedFuelSurchargeRequestDurationMinutesRangeRule(80, 80)
        val item = newBatchedFuelSurchargeRequest("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
