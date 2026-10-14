package com.acme.transport.linehaul.capacity.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityThreshold("code-586", "name-460")
        val view = toCapacityThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityThresholdViews(mutableListOf<CapacityThreshold>(item, item)).size, "views")
    }
}
