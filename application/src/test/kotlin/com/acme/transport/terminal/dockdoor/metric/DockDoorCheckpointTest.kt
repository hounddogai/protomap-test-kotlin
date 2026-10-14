package com.acme.transport.terminal.dockdoor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorCheckpointTest {
    @Test
    fun `starts in planned`() {
        val item = newDockDoorCheckpoint("code-618", "name-125")
        assertEquals(DockDoorCheckpointStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newDockDoorCheckpoint("code-618", "name-125")
        try {
            item.moveTo(DockDoorCheckpointStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorCheckpointStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDockDoorCheckpoint("code-618", "name-125")
        item.status = DockDoorCheckpointStatus.CANCELLED
        assertFails("move") { item.moveTo(DockDoorCheckpointStatus.PLANNED) }
    }
}
