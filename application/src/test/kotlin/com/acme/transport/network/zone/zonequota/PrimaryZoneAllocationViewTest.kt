package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryZoneAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryZoneAllocation("code-348", "title-448")
        val view = toPrimaryZoneAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryZoneAllocationViews(mutableListOf<PrimaryZoneAllocation>(item, item)).size, "views")
    }
}
