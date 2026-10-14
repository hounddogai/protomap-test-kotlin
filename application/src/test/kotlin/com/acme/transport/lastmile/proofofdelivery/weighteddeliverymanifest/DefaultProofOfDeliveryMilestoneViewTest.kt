package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultProofOfDeliveryMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultProofOfDeliveryMilestone("code-705", "name-43")
        val view = toDefaultProofOfDeliveryMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultProofOfDeliveryMilestoneViews(mutableListOf<DefaultProofOfDeliveryMilestone>(item, item)).size, "views")
    }
}
