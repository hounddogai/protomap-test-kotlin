package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRestBreakRecordTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedRestBreakRecord("code-757", "title-371")
        assertEquals(BatchedRestBreakRecordStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newBatchedRestBreakRecord("code-757", "title-371")
        try {
            item.moveTo(BatchedRestBreakRecordStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRestBreakRecordStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedRestBreakRecord("code-757", "title-371")
        item.status = BatchedRestBreakRecordStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedRestBreakRecordStatus.DRAFT) }
    }
}
