package com.acme.transport.network.hub.tieredhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledHubCheckpointTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledHubCheckpoint("code-587", "name-864")
        assertEquals(ScheduledHubCheckpointStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newScheduledHubCheckpoint("code-587", "name-864")
        try {
            item.moveTo(ScheduledHubCheckpointStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledHubCheckpointStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledHubCheckpoint("code-587", "name-864")
        item.status = ScheduledHubCheckpointStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledHubCheckpointStatus.NEW) }
    }
}
