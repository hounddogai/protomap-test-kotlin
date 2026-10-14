package com.acme.transport.linehaul.capacity.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCapacityCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientCapacityCheckpoint("code-145", "label-691")
        assertEquals(LenientCapacityCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newLenientCapacityCheckpoint("code-145", "label-691")
        try {
            item.moveTo(LenientCapacityCheckpointStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCapacityCheckpointStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientCapacityCheckpoint("code-145", "label-691")
        item.status = LenientCapacityCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(LenientCapacityCheckpointStatus.IDLE) }
    }
}
