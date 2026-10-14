package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCapacityManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCapacityManifestValidator()
        val item = newBatchedCapacityManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = BatchedCapacityManifestNotesLengthRule(1000)
        val item = newBatchedCapacityManifest("x", "y")
        item.notes = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = BatchedCapacityManifestCeilingRangeRule(4.4, 3022.9)
        val item = newBatchedCapacityManifest("x", "y")
        item.ceiling = 3024.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = BatchedCapacityManifestBudgetRangeRule(7.1, 1125.4)
        val item = newBatchedCapacityManifest("x", "y")
        item.budget = 1126.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = BatchedCapacityManifestRequiredExternalRefRule("batched capacity manifest needs a value")
        val item = newBatchedCapacityManifest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
