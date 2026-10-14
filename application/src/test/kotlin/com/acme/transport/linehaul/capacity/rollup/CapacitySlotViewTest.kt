package com.acme.transport.linehaul.capacity.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacitySlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacitySlot("code-880", "label-975")
        val view = toCapacitySlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacitySlotViews(mutableListOf<CapacitySlot>(item, item)).size, "views")
    }
}
