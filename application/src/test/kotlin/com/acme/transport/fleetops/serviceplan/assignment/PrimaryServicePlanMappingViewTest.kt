package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryServicePlanMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryServicePlanMapping("code-174", "title-422")
        val view = toPrimaryServicePlanMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryServicePlanMappingViews(mutableListOf<PrimaryServicePlanMapping>(item, item)).size, "views")
    }
}
