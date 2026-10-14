package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDisputeAuditTest {
    @Test
    fun `starts in open`() {
        val item = newLenientDisputeAudit("code-568", "title-609")
        assertEquals(LenientDisputeAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLenientDisputeAudit("code-568", "title-609")
        try {
            item.moveTo(LenientDisputeAuditStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDisputeAuditStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientDisputeAudit("code-568", "title-609")
        item.status = LenientDisputeAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientDisputeAuditStatus.OPEN) }
    }
}
