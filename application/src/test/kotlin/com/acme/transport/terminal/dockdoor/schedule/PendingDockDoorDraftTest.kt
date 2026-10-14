package com.acme.transport.terminal.dockdoor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDockDoorDraftTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingDockDoorDraft("code-659", "title-366")
        assertEquals(PendingDockDoorDraftStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPendingDockDoorDraft("code-659", "title-366")
        try {
            item.moveTo(PendingDockDoorDraftStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDockDoorDraftStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingDockDoorDraft("code-659", "title-366")
        item.status = PendingDockDoorDraftStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingDockDoorDraftStatus.PLANNED) }
    }
}
