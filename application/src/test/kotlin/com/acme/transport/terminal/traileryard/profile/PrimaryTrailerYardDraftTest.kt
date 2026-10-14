package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTrailerYardDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryTrailerYardDraft("code-116", "label-476")
        assertEquals(PrimaryTrailerYardDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryTrailerYardDraft("code-116", "label-476")
        try {
            item.moveTo(PrimaryTrailerYardDraftStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTrailerYardDraftStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryTrailerYardDraft("code-116", "label-476")
        item.status = PrimaryTrailerYardDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryTrailerYardDraftStatus.REQUESTED) }
    }
}
