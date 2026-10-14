package com.acme.transport.fleetops.serviceplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedServicePlanEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedServicePlanEntry("code-465", "title-853")
        val view = toCappedServicePlanEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedServicePlanEntryViews(mutableListOf<CappedServicePlanEntry>(item, item)).size, "views")
    }
}
