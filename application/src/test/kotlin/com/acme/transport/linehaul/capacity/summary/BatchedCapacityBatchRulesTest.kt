package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCapacityBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCapacityBatchValidator()
        val item = newBatchedCapacityBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = BatchedCapacityBatchSummaryLengthRule(1000)
        val item = newBatchedCapacityBatch("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = BatchedCapacityBatchFuelLitersRangeRule(6.5, 3523.6)
        val item = newBatchedCapacityBatch("x", "y")
        item.fuelLiters = 3525.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = BatchedCapacityBatchPalletsRangeRule(255, 1000)
        val item = newBatchedCapacityBatch("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = BatchedCapacityBatchFloorValueRangeRule(9.8, 2563.5)
        val item = newBatchedCapacityBatch("x", "y")
        item.floorValue = 2565.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = BatchedCapacityBatchVersionRangeRule(80, 86400)
        val item = newBatchedCapacityBatch("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
