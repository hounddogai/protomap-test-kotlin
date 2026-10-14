package com.acme.transport.freightpay.audit.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditProfileTest {
    @Test
    fun `starts in open`() {
        val item = newAuditProfile("code-539", "name-962")
        assertEquals(AuditProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAuditProfile("code-539", "name-962")
        try {
            item.moveTo(AuditProfileStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditProfileStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAuditProfile("code-539", "name-962")
        item.status = AuditProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(AuditProfileStatus.OPEN) }
    }
}
