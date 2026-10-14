package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledFuelBurnThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledFuelBurnThreshold("code-678", "name-356")
        val view = toScheduledFuelBurnThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledFuelBurnThresholdViews(mutableListOf<ScheduledFuelBurnThreshold>(item, item)).size, "views")
    }
}
