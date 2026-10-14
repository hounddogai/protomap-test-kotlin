package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanSnapshot("code-254", "label-406")
        val view = toServicePlanSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanSnapshotViews(mutableListOf<ServicePlanSnapshot>(item, item)).size, "views")
    }
}
