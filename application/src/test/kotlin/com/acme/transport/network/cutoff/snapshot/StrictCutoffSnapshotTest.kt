package com.acme.transport.network.cutoff.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCutoffSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newStrictCutoffSnapshot("code-837", "name-575")
        assertEquals(StrictCutoffSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newStrictCutoffSnapshot("code-837", "name-575")
        try {
            item.moveTo(StrictCutoffSnapshotStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCutoffSnapshotStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictCutoffSnapshot("code-837", "name-575")
        item.status = StrictCutoffSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(StrictCutoffSnapshotStatus.NEW) }
    }
}
