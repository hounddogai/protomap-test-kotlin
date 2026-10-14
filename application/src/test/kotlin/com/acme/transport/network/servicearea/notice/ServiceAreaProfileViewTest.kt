package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaProfile("code-86", "name-62")
        val view = toServiceAreaProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaProfileViews(mutableListOf<ServiceAreaProfile>(item, item)).size, "views")
    }
}
