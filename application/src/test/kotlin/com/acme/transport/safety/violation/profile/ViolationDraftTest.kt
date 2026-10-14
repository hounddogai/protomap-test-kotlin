package com.acme.transport.safety.violation.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newViolationDraft("code-70", "label-966")
        assertEquals(ViolationDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newViolationDraft("code-70", "label-966")
        try {
            item.moveTo(ViolationDraftStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationDraftStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newViolationDraft("code-70", "label-966")
        item.status = ViolationDraftStatus.CLOSED
        assertFails("move") { item.moveTo(ViolationDraftStatus.DRAFT) }
    }
}
