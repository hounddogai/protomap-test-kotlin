package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newZoneCheckpoint("code-872", "label-282")
        assertEquals(ZoneCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newZoneCheckpoint("code-872", "label-282")
        try {
            item.moveTo(ZoneCheckpointStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneCheckpointStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newZoneCheckpoint("code-872", "label-282")
        item.status = ZoneCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(ZoneCheckpointStatus.IDLE) }
    }
}
