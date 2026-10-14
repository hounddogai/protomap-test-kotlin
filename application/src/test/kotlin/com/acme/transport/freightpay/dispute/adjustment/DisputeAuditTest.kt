package com.acme.transport.freightpay.dispute.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeAuditTest {
    @Test
    fun `starts in open`() {
        val item = newDisputeAudit("code-460", "name-397")
        assertEquals(DisputeAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDisputeAudit("code-460", "name-397")
        try {
            item.moveTo(DisputeAuditStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeAuditStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDisputeAudit("code-460", "name-397")
        item.status = DisputeAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(DisputeAuditStatus.OPEN) }
    }
}
