package com.acme.transport.linehaul.capacity.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityQuota("code-125", "title-313")
        val view = toCapacityQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityQuotaViews(mutableListOf<CapacityQuota>(item, item)).size, "views")
    }
}
