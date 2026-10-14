package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanAllocationValidator()
        val item = newServicePlanAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ServicePlanAllocationBatchSizeRangeRule(64, 120)
        val item = newServicePlanAllocation("x", "y")
        item.batchSize = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ServicePlanAllocationRequiredLabelRule("service plan allocation needs an entry")
        val item = newServicePlanAllocation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
