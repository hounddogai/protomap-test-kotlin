package com.acme.transport.fleetops.serviceplan.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanPreference("code-168", "name-404")
        val view = toServicePlanPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanPreferenceViews(mutableListOf<ServicePlanPreference>(item, item)).size, "views")
    }
}
