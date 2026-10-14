package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingFuelBurnThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingFuelBurnThreshold("code-500", "label-421")
        val view = toPendingFuelBurnThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingFuelBurnThresholdViews(mutableListOf<PendingFuelBurnThreshold>(item, item)).size, "views")
    }
}
