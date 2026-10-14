package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalAvailabilitySlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalAvailabilitySlot("code-185", "name-364")
        val view = toGlobalAvailabilitySlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalAvailabilitySlotViews(mutableListOf<GlobalAvailabilitySlot>(item, item)).size, "views")
    }
}
