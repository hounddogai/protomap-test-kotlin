package com.acme.transport.network.transittime.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeSlot("code-371", "title-332")
        val view = toTransitTimeSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeSlotViews(mutableListOf<TransitTimeSlot>(item, item)).size, "views")
    }
}
