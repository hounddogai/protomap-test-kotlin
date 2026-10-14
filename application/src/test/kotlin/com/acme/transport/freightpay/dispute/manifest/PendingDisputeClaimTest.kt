package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDisputeClaimTest {
    @Test
    fun `starts in open`() {
        val item = newPendingDisputeClaim("code-413", "name-765")
        assertEquals(PendingDisputeClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPendingDisputeClaim("code-413", "name-765")
        try {
            item.moveTo(PendingDisputeClaimStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDisputeClaimStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingDisputeClaim("code-413", "name-765")
        item.status = PendingDisputeClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingDisputeClaimStatus.OPEN) }
    }
}
