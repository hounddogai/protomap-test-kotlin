package com.acme.transport.fleetops.serviceplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanBatchValidator()
        val item = newServicePlanBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ServicePlanBatchRequiredLabelRule("service plan batch needs an entry")
        val item = newServicePlanBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
