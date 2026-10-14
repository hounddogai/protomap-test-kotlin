package com.acme.transport.terminal.dwell.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newDwellDraft("code-424", "label-406")
        assertEquals(DwellDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDwellDraft("code-424", "label-406")
        try {
            item.moveTo(DwellDraftStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellDraftStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDwellDraft("code-424", "label-406")
        item.status = DwellDraftStatus.CLOSED
        assertFails("move") { item.moveTo(DwellDraftStatus.DRAFT) }
    }
}
