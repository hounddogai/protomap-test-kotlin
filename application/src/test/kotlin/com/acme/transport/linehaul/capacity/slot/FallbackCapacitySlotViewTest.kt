package com.acme.transport.linehaul.capacity.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCapacitySlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCapacitySlot("code-742", "label-906")
        val view = toFallbackCapacitySlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCapacitySlotViews(mutableListOf<FallbackCapacitySlot>(item, item)).size, "views")
    }
}
