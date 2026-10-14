package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCutoffDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledCutoffDraft("code-670", "label-29")
        assertEquals(ScheduledCutoffDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newScheduledCutoffDraft("code-670", "label-29")
        try {
            item.moveTo(ScheduledCutoffDraftStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCutoffDraftStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledCutoffDraft("code-670", "label-29")
        item.status = ScheduledCutoffDraftStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledCutoffDraftStatus.DRAFT) }
    }
}
