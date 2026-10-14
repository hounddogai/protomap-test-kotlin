package com.acme.transport.lastmile.locker.lockerclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackLockerDraftTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackLockerDraft("code-184", "title-624")
        assertEquals(FallbackLockerDraftStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackLockerDraft("code-184", "title-624")
        try {
            item.moveTo(FallbackLockerDraftStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackLockerDraftStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackLockerDraft("code-184", "title-624")
        item.status = FallbackLockerDraftStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackLockerDraftStatus.OPEN) }
    }
}
