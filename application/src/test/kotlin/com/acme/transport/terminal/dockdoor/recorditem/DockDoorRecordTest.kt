package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorRecordTest {
    @Test
    fun `starts in draft`() {
        val item = newDockDoorRecord("code-857", "title-53")
        assertEquals(DockDoorRecordStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newDockDoorRecord("code-857", "title-53")
        try {
            item.moveTo(DockDoorRecordStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorRecordStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDockDoorRecord("code-857", "title-53")
        item.status = DockDoorRecordStatus.CLOSED
        assertFails("move") { item.moveTo(DockDoorRecordStatus.DRAFT) }
    }
}
