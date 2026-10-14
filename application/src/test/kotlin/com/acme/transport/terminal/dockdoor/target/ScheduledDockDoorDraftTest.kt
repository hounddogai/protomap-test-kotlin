package com.acme.transport.terminal.dockdoor.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDockDoorDraftTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledDockDoorDraft("code-357", "title-911")
        assertEquals(ScheduledDockDoorDraftStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledDockDoorDraft("code-357", "title-911")
        try {
            item.moveTo(ScheduledDockDoorDraftStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDockDoorDraftStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledDockDoorDraft("code-357", "title-911")
        item.status = ScheduledDockDoorDraftStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledDockDoorDraftStatus.OPEN) }
    }
}
