package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaCycle("code-877", "name-252")
        val view = toServiceAreaCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaCycleViews(mutableListOf<ServiceAreaCycle>(item, item)).size, "views")
    }
}
