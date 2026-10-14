package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnSegment("code-472", "label-864")
        val view = toFuelBurnSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnSegmentViews(mutableListOf<FuelBurnSegment>(item, item)).size, "views")
    }
}
