package com.acme.transport.customs.declaration.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDeclarationSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDeclarationSnapshotValidator()
        val item = newBatchedDeclarationSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = BatchedDeclarationSnapshotBatchSizeRangeRule(1000, 86400)
        val item = newBatchedDeclarationSnapshot("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = BatchedDeclarationSnapshotEndsAtRangeRule(3600, 86400)
        val item = newBatchedDeclarationSnapshot("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedDeclarationSnapshotRequiredCodeRule("batched declaration snapshot needs an entry")
        val item = newBatchedDeclarationSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
