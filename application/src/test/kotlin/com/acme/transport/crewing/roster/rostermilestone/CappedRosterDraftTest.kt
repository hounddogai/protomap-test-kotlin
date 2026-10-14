package com.acme.transport.crewing.roster.rostermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRosterDraftTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedRosterDraft("code-7", "label-171")
        assertEquals(CappedRosterDraftStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCappedRosterDraft("code-7", "label-171")
        try {
            item.moveTo(CappedRosterDraftStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRosterDraftStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedRosterDraft("code-7", "label-171")
        item.status = CappedRosterDraftStatus.STOPPED
        assertFails("move") { item.moveTo(CappedRosterDraftStatus.IDLE) }
    }
}
