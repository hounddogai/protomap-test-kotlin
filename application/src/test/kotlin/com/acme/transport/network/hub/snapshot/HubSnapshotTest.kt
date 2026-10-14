package com.acme.transport.network.hub.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubSnapshotTest {
    @Test
    fun `starts in planned`() {
        val item = newHubSnapshot("code-322", "title-176")
        assertEquals(HubSnapshotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newHubSnapshot("code-322", "title-176")
        try {
            item.moveTo(HubSnapshotStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubSnapshotStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newHubSnapshot("code-322", "title-176")
        item.status = HubSnapshotStatus.CANCELLED
        assertFails("move") { item.moveTo(HubSnapshotStatus.PLANNED) }
    }
}
