package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newDockDoorSnapshot("code-883", "name-611")
        assertEquals(DockDoorSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDockDoorSnapshot("code-883", "name-611")
        try {
            item.moveTo(DockDoorSnapshotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorSnapshotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDockDoorSnapshot("code-883", "name-611")
        item.status = DockDoorSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(DockDoorSnapshotStatus.CREATED) }
    }
}
