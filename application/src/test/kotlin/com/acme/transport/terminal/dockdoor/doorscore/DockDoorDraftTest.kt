package com.acme.transport.terminal.dockdoor.doorscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorDraftTest {
    @Test
    fun `starts in new`() {
        val item = newDockDoorDraft("code-225", "name-549")
        assertEquals(DockDoorDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDockDoorDraft("code-225", "name-549")
        try {
            item.moveTo(DockDoorDraftStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorDraftStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDockDoorDraft("code-225", "name-549")
        item.status = DockDoorDraftStatus.FAILED
        assertFails("move") { item.moveTo(DockDoorDraftStatus.NEW) }
    }
}
