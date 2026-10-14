package com.acme.transport.linehaul.trailer.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newTrailerDraft("code-844", "name-309")
        assertEquals(TrailerDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newTrailerDraft("code-844", "name-309")
        try {
            item.moveTo(TrailerDraftStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerDraftStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTrailerDraft("code-844", "name-309")
        item.status = TrailerDraftStatus.CLOSED
        assertFails("move") { item.moveTo(TrailerDraftStatus.DRAFT) }
    }
}
