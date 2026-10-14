package com.acme.transport.fleetops.utilization.window

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationQuota("code-697", "label-279")
        val view = toUtilizationQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationQuotaViews(mutableListOf<UtilizationQuota>(item, item)).size, "views")
    }
}
