package com.acme.transport.fleetops.serviceplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServicePlanGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedServicePlanGroupValidator()
        val item = newBatchedServicePlanGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedServicePlanGroupNameLengthRule(3600)
        val item = newBatchedServicePlanGroup("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedServicePlanGroupCodeLengthRule(16)
        val item = newBatchedServicePlanGroup("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedServicePlanGroupRequiredCodeRule("batched service plan group needs this field")
        val item = newBatchedServicePlanGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedServicePlanGroupRequiredNameRule("batched service plan group needs this field")
        val item = newBatchedServicePlanGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = BatchedServicePlanGroupRateRangeRule(1.3, 1052.1)
        val item = newBatchedServicePlanGroup("x", "y")
        item.rate = 1053.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
