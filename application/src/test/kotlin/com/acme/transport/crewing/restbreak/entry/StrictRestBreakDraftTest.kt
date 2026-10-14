package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRestBreakDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictRestBreakDraft("code-287", "name-53")
        assertEquals(StrictRestBreakDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newStrictRestBreakDraft("code-287", "name-53")
        try {
            item.moveTo(StrictRestBreakDraftStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRestBreakDraftStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictRestBreakDraft("code-287", "name-53")
        item.status = StrictRestBreakDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictRestBreakDraftStatus.REQUESTED) }
    }
}
