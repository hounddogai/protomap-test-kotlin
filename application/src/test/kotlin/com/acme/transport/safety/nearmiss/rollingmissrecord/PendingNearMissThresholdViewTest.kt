package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingNearMissThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingNearMissThreshold("code-321", "name-904")
        val view = toPendingNearMissThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingNearMissThresholdViews(mutableListOf<PendingNearMissThreshold>(item, item)).size, "views")
    }
}
