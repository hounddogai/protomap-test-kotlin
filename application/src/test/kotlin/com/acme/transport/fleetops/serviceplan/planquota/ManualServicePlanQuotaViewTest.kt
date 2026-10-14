package com.acme.transport.fleetops.serviceplan.planquota

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualServicePlanQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualServicePlanQuota("code-790", "label-95")
        val view = toManualServicePlanQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualServicePlanQuotaViews(mutableListOf<ManualServicePlanQuota>(item, item)).size, "views")
    }
}
