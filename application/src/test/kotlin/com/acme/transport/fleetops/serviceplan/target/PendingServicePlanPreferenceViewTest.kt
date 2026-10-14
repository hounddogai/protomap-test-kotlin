package com.acme.transport.fleetops.serviceplan.target

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingServicePlanPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingServicePlanPreference("code-735", "title-873")
        val view = toPendingServicePlanPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingServicePlanPreferenceViews(mutableListOf<PendingServicePlanPreference>(item, item)).size, "views")
    }
}
