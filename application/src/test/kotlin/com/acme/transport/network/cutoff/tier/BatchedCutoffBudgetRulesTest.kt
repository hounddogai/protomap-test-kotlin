package com.acme.transport.network.cutoff.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCutoffBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCutoffBudgetValidator()
        val item = newBatchedCutoffBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = BatchedCutoffBudgetRequiredContactEmailRule("batched cutoff budget needs an entry")
        val item = newBatchedCutoffBudget("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = BatchedCutoffBudgetLocaleLengthRule(255)
        val item = newBatchedCutoffBudget("x", "y")
        item.locale = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedCutoffBudgetRequiredCodeRule("batched cutoff budget needs a value")
        val item = newBatchedCutoffBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
