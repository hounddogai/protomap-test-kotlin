package com.acme.transport.network.hub.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubDraftTest {
    @Test
    fun `starts in new`() {
        val item = newHubDraft("code-222", "name-120")
        assertEquals(HubDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newHubDraft("code-222", "name-120")
        try {
            item.moveTo(HubDraftStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubDraftStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newHubDraft("code-222", "name-120")
        item.status = HubDraftStatus.FAILED
        assertFails("move") { item.moveTo(HubDraftStatus.NEW) }
    }
}
