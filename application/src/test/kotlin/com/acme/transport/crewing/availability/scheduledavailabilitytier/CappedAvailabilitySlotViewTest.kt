package com.acme.transport.crewing.availability.scheduledavailabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAvailabilitySlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAvailabilitySlot("code-802", "title-724")
        val view = toCappedAvailabilitySlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAvailabilitySlotViews(mutableListOf<CappedAvailabilitySlot>(item, item)).size, "views")
    }
}
