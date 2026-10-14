package com.acme.transport.crewing.restbreak.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newRestBreakDraft("code-778", "name-364")
        assertEquals(RestBreakDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRestBreakDraft("code-778", "name-364")
        try {
            item.moveTo(RestBreakDraftStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakDraftStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRestBreakDraft("code-778", "name-364")
        item.status = RestBreakDraftStatus.CLOSED
        assertFails("move") { item.moveTo(RestBreakDraftStatus.DRAFT) }
    }
}
