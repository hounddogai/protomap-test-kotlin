package com.acme.transport.linehaul.relay.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayDraftTest {
    @Test
    fun `starts in proposed`() {
        val item = newRelayDraft("code-460", "title-763")
        assertEquals(RelayDraftStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRelayDraft("code-460", "title-763")
        try {
            item.moveTo(RelayDraftStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayDraftStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRelayDraft("code-460", "title-763")
        item.status = RelayDraftStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RelayDraftStatus.PROPOSED) }
    }
}
