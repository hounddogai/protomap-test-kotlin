package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newAuditAllocation("code-394", "title-115")
        assertEquals(AuditAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAuditAllocation("code-394", "title-115")
        try {
            item.moveTo(AuditAllocationStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditAllocationStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAuditAllocation("code-394", "title-115")
        item.status = AuditAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(AuditAllocationStatus.DRAFT) }
    }
}
