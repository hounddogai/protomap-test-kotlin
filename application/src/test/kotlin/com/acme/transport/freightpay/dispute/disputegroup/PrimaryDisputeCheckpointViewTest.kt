package com.acme.transport.freightpay.dispute.disputegroup

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDisputeCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDisputeCheckpoint("code-279", "label-683")
        val view = toPrimaryDisputeCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDisputeCheckpointViews(mutableListOf<PrimaryDisputeCheckpoint>(item, item)).size, "views")
    }
}
