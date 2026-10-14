package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaQuota("code-155", "title-257")
        val view = toServiceAreaQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaQuotaViews(mutableListOf<ServiceAreaQuota>(item, item)).size, "views")
    }
}
