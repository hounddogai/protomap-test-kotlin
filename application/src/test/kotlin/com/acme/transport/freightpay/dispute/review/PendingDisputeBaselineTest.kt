package com.acme.transport.freightpay.dispute.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDisputeBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingDisputeBaseline("code-687", "label-916")
        assertEquals(PendingDisputeBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPendingDisputeBaseline("code-687", "label-916")
        try {
            item.moveTo(PendingDisputeBaselineStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDisputeBaselineStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingDisputeBaseline("code-687", "label-916")
        item.status = PendingDisputeBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(PendingDisputeBaselineStatus.DRAFT) }
    }
}
