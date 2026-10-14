package com.acme.transport.safety.violation.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedViolationAdjustmentTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedViolationAdjustment("code-81", "name-128")
        assertEquals(BatchedViolationAdjustmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newBatchedViolationAdjustment("code-81", "name-128")
        try {
            item.moveTo(BatchedViolationAdjustmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedViolationAdjustmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedViolationAdjustment("code-81", "name-128")
        item.status = BatchedViolationAdjustmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedViolationAdjustmentStatus.OPEN) }
    }
}
