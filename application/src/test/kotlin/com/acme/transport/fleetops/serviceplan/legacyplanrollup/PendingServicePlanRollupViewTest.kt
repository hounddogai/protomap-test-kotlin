package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingServicePlanRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingServicePlanRollup("code-643", "title-572")
        val view = toPendingServicePlanRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingServicePlanRollupViews(mutableListOf<PendingServicePlanRollup>(item, item)).size, "views")
    }
}
