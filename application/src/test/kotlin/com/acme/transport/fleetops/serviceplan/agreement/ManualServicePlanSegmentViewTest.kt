package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualServicePlanSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualServicePlanSegment("code-91", "title-318")
        val view = toManualServicePlanSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualServicePlanSegmentViews(mutableListOf<ManualServicePlanSegment>(item, item)).size, "views")
    }
}
