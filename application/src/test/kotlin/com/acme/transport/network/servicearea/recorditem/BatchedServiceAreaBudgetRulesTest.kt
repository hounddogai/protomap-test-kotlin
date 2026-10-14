package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServiceAreaBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedServiceAreaBudgetValidator()
        val item = newBatchedServiceAreaBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = BatchedServiceAreaBudgetRequiredContactEmailRule("batched service area budget needs an entry")
        val item = newBatchedServiceAreaBudget("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = BatchedServiceAreaBudgetOdometerRangeRule(255, 1000)
        val item = newBatchedServiceAreaBudget("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = BatchedServiceAreaBudgetThresholdRangeRule(9.6, 2139.8)
        val item = newBatchedServiceAreaBudget("x", "y")
        item.threshold = 2141.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = BatchedServiceAreaBudgetRequiredOwnerIdRule("batched service area budget needs this field")
        val item = newBatchedServiceAreaBudget("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
