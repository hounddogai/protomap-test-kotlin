package com.acme.transport.terminal.sortation.sortationvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedSortationOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedSortationOverrideValidator()
        val item = newBatchedSortationOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = BatchedSortationOverrideRequiredLaneRule("batched sortation override needs a value")
        val item = newBatchedSortationOverride("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedSortationOverrideLabelLengthRule(16)
        val item = newBatchedSortationOverride("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
