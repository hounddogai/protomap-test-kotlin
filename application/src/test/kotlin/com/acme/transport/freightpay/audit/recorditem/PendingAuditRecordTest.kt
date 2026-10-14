package com.acme.transport.freightpay.audit.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAuditRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingAuditRecord("code-548", "name-431")
        assertEquals(PendingAuditRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingAuditRecord("code-548", "name-431")
        try {
            item.moveTo(PendingAuditRecordStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAuditRecordStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingAuditRecord("code-548", "name-431")
        item.status = PendingAuditRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingAuditRecordStatus.REQUESTED) }
    }
}
