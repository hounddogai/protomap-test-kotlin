package com.acme.transport.network.servicearea.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalServiceAreaSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newGlobalServiceAreaSnapshot("code-662", "name-852")
        assertEquals(GlobalServiceAreaSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newGlobalServiceAreaSnapshot("code-662", "name-852")
        try {
            item.moveTo(GlobalServiceAreaSnapshotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalServiceAreaSnapshotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newGlobalServiceAreaSnapshot("code-662", "name-852")
        item.status = GlobalServiceAreaSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(GlobalServiceAreaSnapshotStatus.NEW) }
    }
}
