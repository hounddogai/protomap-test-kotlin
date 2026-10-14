package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingServicePlanMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingServicePlanMapping("code-836", "label-85")
        val view = toPendingServicePlanMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingServicePlanMappingViews(mutableListOf<PendingServicePlanMapping>(item, item)).size, "views")
    }
}
