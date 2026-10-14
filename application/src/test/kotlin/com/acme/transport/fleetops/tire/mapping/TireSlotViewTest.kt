package com.acme.transport.fleetops.tire.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class TireSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireSlot("code-14", "label-820")
        val view = toTireSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireSlotViews(mutableListOf<TireSlot>(item, item)).size, "views")
    }
}
