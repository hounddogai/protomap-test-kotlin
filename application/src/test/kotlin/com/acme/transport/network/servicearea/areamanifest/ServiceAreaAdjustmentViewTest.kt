package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaAdjustment("code-133", "title-578")
        val view = toServiceAreaAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaAdjustmentViews(mutableListOf<ServiceAreaAdjustment>(item, item)).size, "views")
    }
}
