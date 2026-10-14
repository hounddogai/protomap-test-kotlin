package com.acme.transport.terminal.traileryard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardDraftTest {
    @Test
    fun `starts in new`() {
        val item = newTrailerYardDraft("code-196", "title-165")
        assertEquals(TrailerYardDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTrailerYardDraft("code-196", "title-165")
        try {
            item.moveTo(TrailerYardDraftStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardDraftStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTrailerYardDraft("code-196", "title-165")
        item.status = TrailerYardDraftStatus.FAILED
        assertFails("move") { item.moveTo(TrailerYardDraftStatus.NEW) }
    }
}
