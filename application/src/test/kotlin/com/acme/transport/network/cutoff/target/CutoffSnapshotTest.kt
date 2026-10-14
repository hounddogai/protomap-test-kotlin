package com.acme.transport.network.cutoff.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newCutoffSnapshot("code-797", "title-407")
        assertEquals(CutoffSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCutoffSnapshot("code-797", "title-407")
        try {
            item.moveTo(CutoffSnapshotStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffSnapshotStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCutoffSnapshot("code-797", "title-407")
        item.status = CutoffSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(CutoffSnapshotStatus.IDLE) }
    }
}
