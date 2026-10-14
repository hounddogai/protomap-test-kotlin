package com.acme.transport.network.servicearea.request

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaRequest("code-532", "label-852")
        val view = toServiceAreaRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaRequestViews(mutableListOf<ServiceAreaRequest>(item, item)).size, "views")
    }
}
