package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveClearanceEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveClearanceEstimate("code-926", "label-703")
        val view = toAdaptiveClearanceEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveClearanceEstimateViews(mutableListOf<AdaptiveClearanceEstimate>(item, item)).size, "views")
    }
}
