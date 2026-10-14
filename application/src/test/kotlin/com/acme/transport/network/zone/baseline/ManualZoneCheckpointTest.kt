package com.acme.transport.network.zone.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualZoneCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newManualZoneCheckpoint("code-254", "title-696")
        assertEquals(ManualZoneCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualZoneCheckpoint("code-254", "title-696")
        try {
            item.moveTo(ManualZoneCheckpointStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualZoneCheckpointStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualZoneCheckpoint("code-254", "title-696")
        item.status = ManualZoneCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(ManualZoneCheckpointStatus.IDLE) }
    }
}
