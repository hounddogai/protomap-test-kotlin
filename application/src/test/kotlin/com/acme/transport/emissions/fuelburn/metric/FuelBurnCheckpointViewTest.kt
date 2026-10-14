package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnCheckpoint("code-287", "name-705")
        val view = toFuelBurnCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnCheckpointViews(mutableListOf<FuelBurnCheckpoint>(item, item)).size, "views")
    }
}
