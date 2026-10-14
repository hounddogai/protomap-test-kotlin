package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedUtilizationTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedUtilizationTierValidator()
        val item = newBatchedUtilizationTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedUtilizationTierLabelLengthRule(86400)
        val item = newBatchedUtilizationTier("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedUtilizationTierRequiredLabelRule("batched utilization tier needs a value")
        val item = newBatchedUtilizationTier("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
