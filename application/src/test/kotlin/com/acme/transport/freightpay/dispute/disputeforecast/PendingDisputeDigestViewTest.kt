package com.acme.transport.freightpay.dispute.disputeforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDisputeDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDisputeDigest("code-8", "title-503")
        val view = toPendingDisputeDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDisputeDigestViews(mutableListOf<PendingDisputeDigest>(item, item)).size, "views")
    }
}
