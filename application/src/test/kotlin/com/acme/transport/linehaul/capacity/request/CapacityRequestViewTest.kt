package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityRequest("code-428", "title-86")
        val view = toCapacityRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityRequestViews(mutableListOf<CapacityRequest>(item, item)).size, "views")
    }
}
