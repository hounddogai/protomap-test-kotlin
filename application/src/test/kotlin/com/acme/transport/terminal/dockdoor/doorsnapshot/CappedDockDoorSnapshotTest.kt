package com.acme.transport.terminal.dockdoor.doorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDockDoorSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedDockDoorSnapshot("code-398", "label-279")
        assertEquals(CappedDockDoorSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCappedDockDoorSnapshot("code-398", "label-279")
        try {
            item.moveTo(CappedDockDoorSnapshotStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDockDoorSnapshotStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedDockDoorSnapshot("code-398", "label-279")
        item.status = CappedDockDoorSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(CappedDockDoorSnapshotStatus.IDLE) }
    }
}
