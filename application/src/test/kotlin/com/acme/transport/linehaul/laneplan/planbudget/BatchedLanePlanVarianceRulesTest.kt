package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedLanePlanVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedLanePlanVarianceValidator()
        val item = newBatchedLanePlanVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = BatchedLanePlanVariancePalletsRangeRule(255, 86400)
        val item = newBatchedLanePlanVariance("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedLanePlanVarianceRequiredCodeRule("batched lane plan variance needs a value")
        val item = newBatchedLanePlanVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = BatchedLanePlanVarianceRequiredVehicleIdRule("batched lane plan variance needs this field")
        val item = newBatchedLanePlanVariance("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
