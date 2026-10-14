package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDockDoorDraftTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyDockDoorDraft("code-444", "title-460")
        assertEquals(LegacyDockDoorDraftStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLegacyDockDoorDraft("code-444", "title-460")
        try {
            item.moveTo(LegacyDockDoorDraftStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDockDoorDraftStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyDockDoorDraft("code-444", "title-460")
        item.status = LegacyDockDoorDraftStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyDockDoorDraftStatus.IDLE) }
    }
}
