package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingNodeDraftTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingNodeDraft("code-445", "title-608")
        assertEquals(RollingNodeDraftStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingNodeDraft("code-445", "title-608")
        try {
            item.moveTo(RollingNodeDraftStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingNodeDraftStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingNodeDraft("code-445", "title-608")
        item.status = RollingNodeDraftStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingNodeDraftStatus.PROPOSED) }
    }
}
