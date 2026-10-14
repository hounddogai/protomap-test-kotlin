package com.acme.transport.network.transittime.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTransitTimeCheckpointTest {
    @Test
    fun `starts in new`() {
        val item = newPendingTransitTimeCheckpoint("code-653", "title-925")
        assertEquals(PendingTransitTimeCheckpointStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newPendingTransitTimeCheckpoint("code-653", "title-925")
        try {
            item.moveTo(PendingTransitTimeCheckpointStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTransitTimeCheckpointStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingTransitTimeCheckpoint("code-653", "title-925")
        item.status = PendingTransitTimeCheckpointStatus.FAILED
        assertFails("move") { item.moveTo(PendingTransitTimeCheckpointStatus.NEW) }
    }
}
