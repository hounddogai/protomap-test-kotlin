package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDockDoorBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedDockDoorBaseline("code-128", "title-135")
        assertEquals(BatchedDockDoorBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBatchedDockDoorBaseline("code-128", "title-135")
        try {
            item.moveTo(BatchedDockDoorBaselineStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDockDoorBaselineStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedDockDoorBaseline("code-128", "title-135")
        item.status = BatchedDockDoorBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedDockDoorBaselineStatus.IDLE) }
    }
}
