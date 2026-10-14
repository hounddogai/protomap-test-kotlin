package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedDisputeSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedDisputeSnapshot("code-299", "label-605")
        val view = toWeightedDisputeSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedDisputeSnapshotViews(mutableListOf<WeightedDisputeSnapshot>(item, item)).size, "views")
    }
}
