package com.acme.transport.freightpay.dispute.disputeforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDisputeDigestTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingDisputeDigest("code-8", "title-503")
        assertEquals(PendingDisputeDigestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newPendingDisputeDigest("code-8", "title-503")
        try {
            item.moveTo(PendingDisputeDigestStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDisputeDigestStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingDisputeDigest("code-8", "title-503")
        item.status = PendingDisputeDigestStatus.CLOSED
        assertFails("move") { item.moveTo(PendingDisputeDigestStatus.DRAFT) }
    }
}
