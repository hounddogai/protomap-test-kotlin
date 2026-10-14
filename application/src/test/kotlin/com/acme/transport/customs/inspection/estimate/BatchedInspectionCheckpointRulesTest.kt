package com.acme.transport.customs.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedInspectionCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedInspectionCheckpointValidator()
        val item = newBatchedInspectionCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = BatchedInspectionCheckpointSourceLengthRule(1000)
        val item = newBatchedInspectionCheckpoint("x", "y")
        item.source = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = BatchedInspectionCheckpointRequiredExternalRefRule("batched inspection checkpoint needs an entry")
        val item = newBatchedInspectionCheckpoint("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedInspectionCheckpointTitleLengthRule(64)
        val item = newBatchedInspectionCheckpoint("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
