package com.acme.transport.freightpay.dispute.disputemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDisputeManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingDisputeManifest("code-725", "label-991")
        assertEquals(PendingDisputeManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newPendingDisputeManifest("code-725", "label-991")
        try {
            item.moveTo(PendingDisputeManifestStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDisputeManifestStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingDisputeManifest("code-725", "label-991")
        item.status = PendingDisputeManifestStatus.CLOSED
        assertFails("move") { item.moveTo(PendingDisputeManifestStatus.DRAFT) }
    }
}
