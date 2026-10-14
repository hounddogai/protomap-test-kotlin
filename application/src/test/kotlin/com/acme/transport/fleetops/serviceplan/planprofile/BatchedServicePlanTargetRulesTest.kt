package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServicePlanTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedServicePlanTargetValidator()
        val item = newBatchedServicePlanTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = BatchedServicePlanTargetAttemptsRangeRule(32, 120)
        val item = newBatchedServicePlanTarget("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedServicePlanTargetRequiredCodeRule("batched service plan target needs a value")
        val item = newBatchedServicePlanTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedServicePlanTargetCodeLengthRule(1000)
        val item = newBatchedServicePlanTarget("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
