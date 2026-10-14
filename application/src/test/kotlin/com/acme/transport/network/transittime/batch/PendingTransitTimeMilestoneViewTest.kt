package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingTransitTimeMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingTransitTimeMilestone("code-850", "name-116")
        val view = toPendingTransitTimeMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingTransitTimeMilestoneViews(mutableListOf<PendingTransitTimeMilestone>(item, item)).size, "views")
    }
}
