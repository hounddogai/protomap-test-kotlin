package com.acme.transport.network.hub.rollinghubentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledHubSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledHubSnapshot("code-450", "label-400")
        assertEquals(ScheduledHubSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newScheduledHubSnapshot("code-450", "label-400")
        try {
            item.moveTo(ScheduledHubSnapshotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledHubSnapshotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledHubSnapshot("code-450", "label-400")
        item.status = ScheduledHubSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledHubSnapshotStatus.NEW) }
    }
}
