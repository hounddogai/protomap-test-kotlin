package com.acme.transport.fleetops.utilization.tieredutilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyUtilizationSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyUtilizationSegment("code-29", "name-195")
        val view = toLegacyUtilizationSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyUtilizationSegmentViews(mutableListOf<LegacyUtilizationSegment>(item, item)).size, "views")
    }
}
