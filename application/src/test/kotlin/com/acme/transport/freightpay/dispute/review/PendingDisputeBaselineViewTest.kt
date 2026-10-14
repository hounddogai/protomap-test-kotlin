package com.acme.transport.freightpay.dispute.review

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDisputeBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDisputeBaseline("code-687", "label-916")
        val view = toPendingDisputeBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDisputeBaselineViews(mutableListOf<PendingDisputeBaseline>(item, item)).size, "views")
    }
}
