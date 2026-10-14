package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newZoneSnapshot("code-52", "title-268")
        assertEquals(ZoneSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newZoneSnapshot("code-52", "title-268")
        try {
            item.moveTo(ZoneSnapshotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneSnapshotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newZoneSnapshot("code-52", "title-268")
        item.status = ZoneSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(ZoneSnapshotStatus.NEW) }
    }
}
