package com.acme.transport.fleetops.serviceplan.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientServicePlanSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientServicePlanSummary("code-709", "name-22")
        val view = toLenientServicePlanSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientServicePlanSummaryViews(mutableListOf<LenientServicePlanSummary>(item, item)).size, "views")
    }
}
