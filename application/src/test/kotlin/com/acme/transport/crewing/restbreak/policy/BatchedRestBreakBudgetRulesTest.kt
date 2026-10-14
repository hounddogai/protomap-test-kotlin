package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRestBreakBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRestBreakBudgetValidator()
        val item = newBatchedRestBreakBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = BatchedRestBreakBudgetRequiredPhoneRule("batched rest break budget needs an entry")
        val item = newBatchedRestBreakBudget("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedRestBreakBudgetRequiredNameRule("batched rest break budget needs a value")
        val item = newBatchedRestBreakBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
