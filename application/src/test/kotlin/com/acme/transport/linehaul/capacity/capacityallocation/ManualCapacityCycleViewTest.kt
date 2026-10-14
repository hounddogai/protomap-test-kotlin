package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCapacityCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCapacityCycle("code-455", "title-331")
        val view = toManualCapacityCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCapacityCycleViews(mutableListOf<ManualCapacityCycle>(item, item)).size, "views")
    }
}
