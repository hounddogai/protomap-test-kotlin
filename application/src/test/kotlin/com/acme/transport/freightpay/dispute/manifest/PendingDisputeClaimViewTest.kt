package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDisputeClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDisputeClaim("code-413", "name-765")
        val view = toPendingDisputeClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDisputeClaimViews(mutableListOf<PendingDisputeClaim>(item, item)).size, "views")
    }
}
