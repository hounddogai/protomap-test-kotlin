package com.acme.transport.safety.cameraevent.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCameraEventProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCameraEventProposal("code-197", "name-683")
        val view = toPendingCameraEventProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCameraEventProposalViews(mutableListOf<PendingCameraEventProposal>(item, item)).size, "views")
    }
}
