package com.acme.transport.lastmile.proofofdelivery.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingProofOfDeliveryMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingProofOfDeliveryMilestone("code-444", "label-145")
        val view = toPendingProofOfDeliveryMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingProofOfDeliveryMilestoneViews(mutableListOf<PendingProofOfDeliveryMilestone>(item, item)).size, "views")
    }
}
