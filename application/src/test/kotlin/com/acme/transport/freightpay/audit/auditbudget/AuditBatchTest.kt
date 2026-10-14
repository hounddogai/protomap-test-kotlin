package com.acme.transport.freightpay.audit.auditbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditBatchTest {
    @Test
    fun `starts in open`() {
        val item = newAuditBatch("code-703", "label-825")
        assertEquals(AuditBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAuditBatch("code-703", "label-825")
        try {
            item.moveTo(AuditBatchStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditBatchStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAuditBatch("code-703", "label-825")
        item.status = AuditBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(AuditBatchStatus.OPEN) }
    }
}
