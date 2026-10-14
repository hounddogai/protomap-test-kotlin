package com.acme.transport.emissions.fuelburn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualFuelBurnThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualFuelBurnThreshold("code-650", "title-472")
        val view = toManualFuelBurnThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualFuelBurnThresholdViews(mutableListOf<ManualFuelBurnThreshold>(item, item)).size, "views")
    }
}
