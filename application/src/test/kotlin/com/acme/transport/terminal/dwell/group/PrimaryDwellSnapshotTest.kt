package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDwellSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryDwellSnapshot("code-518", "label-79")
        assertEquals(PrimaryDwellSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPrimaryDwellSnapshot("code-518", "label-79")
        try {
            item.moveTo(PrimaryDwellSnapshotStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDwellSnapshotStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryDwellSnapshot("code-518", "label-79")
        item.status = PrimaryDwellSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryDwellSnapshotStatus.IDLE) }
    }
}
