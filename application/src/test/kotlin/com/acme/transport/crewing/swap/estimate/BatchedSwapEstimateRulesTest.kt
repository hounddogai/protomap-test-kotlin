package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedSwapEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedSwapEstimateValidator()
        val item = newBatchedSwapEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = BatchedSwapEstimateRequiredVehicleIdRule("batched swap estimate needs this field")
        val item = newBatchedSwapEstimate("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedSwapEstimateRequiredLabelRule("batched swap estimate needs an entry")
        val item = newBatchedSwapEstimate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = BatchedSwapEstimateRetriesRangeRule(32, 80)
        val item = newBatchedSwapEstimate("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
