package com.acme.transport.lastmile.attempt.attemptdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAttemptDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalAttemptDraft("code-570", "title-116")
        assertEquals(GlobalAttemptDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newGlobalAttemptDraft("code-570", "title-116")
        try {
            item.moveTo(GlobalAttemptDraftStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAttemptDraftStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalAttemptDraft("code-570", "title-116")
        item.status = GlobalAttemptDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalAttemptDraftStatus.REQUESTED) }
    }
}
