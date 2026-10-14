package com.acme.transport.fleetops.serviceplan.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientServicePlanQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientServicePlanQuota("code-404", "label-386")
        val view = toLenientServicePlanQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientServicePlanQuotaViews(mutableListOf<LenientServicePlanQuota>(item, item)).size, "views")
    }
}
