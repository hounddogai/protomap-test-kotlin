package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCapacityBatchTest {
    @Test
    fun `starts in new`() {
        val item = newTieredCapacityBatch("code-850", "title-123")
        assertEquals(TieredCapacityBatchStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTieredCapacityBatch("code-850", "title-123")
        try {
            item.moveTo(TieredCapacityBatchStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCapacityBatchStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredCapacityBatch("code-850", "title-123")
        item.status = TieredCapacityBatchStatus.FAILED
        assertFails("move") { item.moveTo(TieredCapacityBatchStatus.NEW) }
    }
}
