package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDockDoorCheckpointTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedDockDoorCheckpoint("code-59", "title-176")
        assertEquals(BatchedDockDoorCheckpointStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBatchedDockDoorCheckpoint("code-59", "title-176")
        try {
            item.moveTo(BatchedDockDoorCheckpointStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDockDoorCheckpointStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedDockDoorCheckpoint("code-59", "title-176")
        item.status = BatchedDockDoorCheckpointStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedDockDoorCheckpointStatus.PLANNED) }
    }
}
