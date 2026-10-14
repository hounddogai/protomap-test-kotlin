package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnBatch("code-53", "name-531")
        val view = toFuelBurnBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnBatchViews(mutableListOf<FuelBurnBatch>(item, item)).size, "views")
    }
}
