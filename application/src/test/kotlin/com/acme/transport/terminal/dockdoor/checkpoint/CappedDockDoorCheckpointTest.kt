package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDockDoorCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newCappedDockDoorCheckpoint("code-331", "title-457")
        assertEquals(CappedDockDoorCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newCappedDockDoorCheckpoint("code-331", "title-457")
        try {
            item.moveTo(CappedDockDoorCheckpointStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDockDoorCheckpointStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedDockDoorCheckpoint("code-331", "title-457")
        item.status = CappedDockDoorCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(CappedDockDoorCheckpointStatus.CREATED) }
    }
}
