package com.acme.transport.fleetops.tire.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class TireQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireQuota("code-315", "label-925")
        val view = toTireQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireQuotaViews(mutableListOf<TireQuota>(item, item)).size, "views")
    }
}
