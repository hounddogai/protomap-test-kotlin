package com.acme.transport.customs.declaration.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDeclarationSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedDeclarationSnapshot("code-221", "label-553")
        assertEquals(BatchedDeclarationSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBatchedDeclarationSnapshot("code-221", "label-553")
        try {
            item.moveTo(BatchedDeclarationSnapshotStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDeclarationSnapshotStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedDeclarationSnapshot("code-221", "label-553")
        item.status = BatchedDeclarationSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedDeclarationSnapshotStatus.IDLE) }
    }
}
