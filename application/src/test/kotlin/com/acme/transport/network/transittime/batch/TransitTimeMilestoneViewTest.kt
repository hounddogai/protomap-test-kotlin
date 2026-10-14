package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeMilestone("code-282", "label-813")
        val view = toTransitTimeMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeMilestoneViews(mutableListOf<TransitTimeMilestone>(item, item)).size, "views")
    }
}
