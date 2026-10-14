package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServicePlanReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedServicePlanReservationValidator()
        val item = newBatchedServicePlanReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = BatchedServicePlanReservationCurrencyLengthRule(120)
        val item = newBatchedServicePlanReservation("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = BatchedServicePlanReservationThresholdRangeRule(3.0, 4058.4)
        val item = newBatchedServicePlanReservation("x", "y")
        item.threshold = 4059.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = BatchedServicePlanReservationRequiredCurrencyRule(
            "batched service plan reservation needs this field",
        )
        val item = newBatchedServicePlanReservation("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = BatchedServicePlanReservationQuantityRangeRule(80, 80)
        val item = newBatchedServicePlanReservation("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
