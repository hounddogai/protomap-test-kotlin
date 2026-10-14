package com.acme.transport.network.servicearea.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaSlot("code-891", "label-94")
        val view = toServiceAreaSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaSlotViews(mutableListOf<ServiceAreaSlot>(item, item)).size, "views")
    }
}
