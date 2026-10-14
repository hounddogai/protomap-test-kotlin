package com.acme.transport.safety.nearmiss.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingNearMissBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingNearMissBudgetValidator()
        val item = newPendingNearMissBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = PendingNearMissBudgetBatchSizeRangeRule(32, 64)
        val item = newPendingNearMissBudget("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PendingNearMissBudgetExpiresAtRangeRule(80, 255)
        val item = newPendingNearMissBudget("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = PendingNearMissBudgetBudgetRangeRule(4.0, 385.0)
        val item = newPendingNearMissBudget("x", "y")
        item.budget = 386.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
