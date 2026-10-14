package com.acme.transport.freightpay.audit.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newAuditClaim("code-2", "label-698")
        assertEquals(AuditClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newAuditClaim("code-2", "label-698")
        try {
            item.moveTo(AuditClaimStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditClaimStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAuditClaim("code-2", "label-698")
        item.status = AuditClaimStatus.CLOSED
        assertFails("move") { item.moveTo(AuditClaimStatus.DRAFT) }
    }
}
