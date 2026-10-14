package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingZoneCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingZoneCheckpoint("code-870", "title-634")
        assertEquals(RollingZoneCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRollingZoneCheckpoint("code-870", "title-634")
        try {
            item.moveTo(RollingZoneCheckpointStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingZoneCheckpointStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingZoneCheckpoint("code-870", "title-634")
        item.status = RollingZoneCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(RollingZoneCheckpointStatus.IDLE) }
    }
}
