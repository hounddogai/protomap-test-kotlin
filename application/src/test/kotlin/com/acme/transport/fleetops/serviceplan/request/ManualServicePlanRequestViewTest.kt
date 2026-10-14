package com.acme.transport.fleetops.serviceplan.request

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualServicePlanRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualServicePlanRequest("code-881", "title-405")
        val view = toManualServicePlanRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualServicePlanRequestViews(mutableListOf<ManualServicePlanRequest>(item, item)).size, "views")
    }
}
