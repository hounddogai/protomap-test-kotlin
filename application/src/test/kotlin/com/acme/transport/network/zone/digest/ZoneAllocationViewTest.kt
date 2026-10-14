package com.acme.transport.network.zone.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneAllocation("code-733", "title-761")
        val view = toZoneAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneAllocationViews(mutableListOf<ZoneAllocation>(item, item)).size, "views")
    }
}
