package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeCheckpoint("code-935", "label-386")
        val view = toDisputeCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeCheckpointViews(mutableListOf<DisputeCheckpoint>(item, item)).size, "views")
    }
}
