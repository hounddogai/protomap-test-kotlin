package com.acme.transport.terminal.crossdock.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCrossDockEntryTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedCrossDockEntry("code-923", "label-465")
        assertEquals(BatchedCrossDockEntryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBatchedCrossDockEntry("code-923", "label-465")
        try {
            item.moveTo(BatchedCrossDockEntryStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCrossDockEntryStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedCrossDockEntry("code-923", "label-465")
        item.status = BatchedCrossDockEntryStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedCrossDockEntryStatus.IDLE) }
    }
}
