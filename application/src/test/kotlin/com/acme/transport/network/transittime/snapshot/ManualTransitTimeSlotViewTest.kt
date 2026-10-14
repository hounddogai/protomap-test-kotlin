package com.acme.transport.network.transittime.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTransitTimeSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTransitTimeSlot("code-509", "name-723")
        val view = toManualTransitTimeSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTransitTimeSlotViews(mutableListOf<ManualTransitTimeSlot>(item, item)).size, "views")
    }
}
