package com.acme.transport.network.zone.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledZoneCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledZoneCheckpoint("code-774", "name-381")
        assertEquals(ScheduledZoneCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newScheduledZoneCheckpoint("code-774", "name-381")
        try {
            item.moveTo(ScheduledZoneCheckpointStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledZoneCheckpointStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledZoneCheckpoint("code-774", "name-381")
        item.status = ScheduledZoneCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledZoneCheckpointStatus.IDLE) }
    }
}
