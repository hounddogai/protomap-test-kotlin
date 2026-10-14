package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryServicePlanProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryServicePlanProfile("code-874", "label-361")
        val view = toPrimaryServicePlanProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryServicePlanProfileViews(mutableListOf<PrimaryServicePlanProfile>(item, item)).size, "views")
    }
}
