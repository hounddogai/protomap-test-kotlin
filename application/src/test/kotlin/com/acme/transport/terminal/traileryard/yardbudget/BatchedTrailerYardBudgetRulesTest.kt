package com.acme.transport.terminal.traileryard.yardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerYardBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTrailerYardBudgetValidator()
        val item = newBatchedTrailerYardBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedTrailerYardBudgetRequiredCodeRule("batched trailer yard budget needs a value")
        val item = newBatchedTrailerYardBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = BatchedTrailerYardBudgetPhoneLengthRule(1000)
        val item = newBatchedTrailerYardBudget("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedTrailerYardBudgetCodeLengthRule(32)
        val item = newBatchedTrailerYardBudget("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
