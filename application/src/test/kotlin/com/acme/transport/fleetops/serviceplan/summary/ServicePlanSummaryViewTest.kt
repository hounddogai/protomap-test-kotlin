package com.acme.transport.fleetops.serviceplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanSummary("code-192", "name-787")
        val view = toServicePlanSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanSummaryViews(mutableListOf<ServicePlanSummary>(item, item)).size, "views")
    }
}
