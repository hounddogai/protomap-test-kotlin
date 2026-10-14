package com.acme.transport.fleetops.utilization.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalUtilizationSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalUtilizationSegment("code-102", "label-679")
        val view = toRegionalUtilizationSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalUtilizationSegmentViews(mutableListOf<RegionalUtilizationSegment>(item, item)).size, "views")
    }
}
