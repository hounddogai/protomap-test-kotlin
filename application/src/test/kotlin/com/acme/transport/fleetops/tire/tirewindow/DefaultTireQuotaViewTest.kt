package com.acme.transport.fleetops.tire.tirewindow

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTireQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTireQuota("code-465", "label-692")
        val view = toDefaultTireQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTireQuotaViews(mutableListOf<DefaultTireQuota>(item, item)).size, "views")
    }
}
