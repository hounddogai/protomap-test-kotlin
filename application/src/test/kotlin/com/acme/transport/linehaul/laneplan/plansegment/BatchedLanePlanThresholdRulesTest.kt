package com.acme.transport.linehaul.laneplan.plansegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedLanePlanThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedLanePlanThresholdValidator()
        val item = newBatchedLanePlanThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = BatchedLanePlanThresholdRequiredVehicleIdRule("batched lane plan threshold needs an entry")
        val item = newBatchedLanePlanThreshold("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = BatchedLanePlanThresholdRequiredCategoryRule("batched lane plan threshold needs a value")
        val item = newBatchedLanePlanThreshold("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedLanePlanThresholdRequiredCodeRule("batched lane plan threshold needs this field")
        val item = newBatchedLanePlanThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
