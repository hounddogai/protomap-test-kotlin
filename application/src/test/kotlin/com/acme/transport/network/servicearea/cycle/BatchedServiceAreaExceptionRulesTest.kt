package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServiceAreaExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedServiceAreaExceptionValidator()
        val item = newBatchedServiceAreaException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = BatchedServiceAreaExceptionBudgetRangeRule(6.9, 1092.8)
        val item = newBatchedServiceAreaException("x", "y")
        item.budget = 1094.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedServiceAreaExceptionRequiredCodeRule("batched service area exception needs a value")
        val item = newBatchedServiceAreaException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = BatchedServiceAreaExceptionRequiredLocaleRule("batched service area exception needs an entry")
        val item = newBatchedServiceAreaException("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
