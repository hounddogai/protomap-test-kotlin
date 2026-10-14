package com.acme.transport.freightpay.audit.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newAuditDraft("code-225", "name-122")
        assertEquals(AuditDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newAuditDraft("code-225", "name-122")
        try {
            item.moveTo(AuditDraftStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditDraftStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAuditDraft("code-225", "name-122")
        item.status = AuditDraftStatus.CLOSED
        assertFails("move") { item.moveTo(AuditDraftStatus.DRAFT) }
    }
}
