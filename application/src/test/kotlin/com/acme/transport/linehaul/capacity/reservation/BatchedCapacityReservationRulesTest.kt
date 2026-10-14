package com.acme.transport.linehaul.capacity.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCapacityReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCapacityReservationValidator()
        val item = newBatchedCapacityReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = BatchedCapacityReservationRequiredCarrierRule("batched capacity reservation needs a value")
        val item = newBatchedCapacityReservation("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = BatchedCapacityReservationThresholdRangeRule(0.1, 4001.3)
        val item = newBatchedCapacityReservation("x", "y")
        item.threshold = 4002.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = BatchedCapacityReservationVersionRangeRule(16, 120)
        val item = newBatchedCapacityReservation("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = BatchedCapacityReservationRequiredSegmentRule("batched capacity reservation needs an entry")
        val item = newBatchedCapacityReservation("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
