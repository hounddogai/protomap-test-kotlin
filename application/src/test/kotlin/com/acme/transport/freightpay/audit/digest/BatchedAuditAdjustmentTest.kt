package com.acme.transport.freightpay.audit.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAuditAdjustmentTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedAuditAdjustment("code-628", "title-985")
        assertEquals(BatchedAuditAdjustmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newBatchedAuditAdjustment("code-628", "title-985")
        try {
            item.moveTo(BatchedAuditAdjustmentStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAuditAdjustmentStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedAuditAdjustment("code-628", "title-985")
        item.status = BatchedAuditAdjustmentStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedAuditAdjustmentStatus.PLANNED) }
    }
}
