package com.acme.transport.network.hub.hubdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalHubDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalHubDraft("code-683", "name-149")
        assertEquals(GlobalHubDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newGlobalHubDraft("code-683", "name-149")
        try {
            item.moveTo(GlobalHubDraftStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalHubDraftStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalHubDraft("code-683", "name-149")
        item.status = GlobalHubDraftStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalHubDraftStatus.DRAFT) }
    }
}
