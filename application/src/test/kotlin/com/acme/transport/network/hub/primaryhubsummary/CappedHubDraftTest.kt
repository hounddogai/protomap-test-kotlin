package com.acme.transport.network.hub.primaryhubsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedHubDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedHubDraft("code-313", "label-475")
        assertEquals(CappedHubDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newCappedHubDraft("code-313", "label-475")
        try {
            item.moveTo(CappedHubDraftStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedHubDraftStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedHubDraft("code-313", "label-475")
        item.status = CappedHubDraftStatus.CLOSED
        assertFails("move") { item.moveTo(CappedHubDraftStatus.DRAFT) }
    }
}
