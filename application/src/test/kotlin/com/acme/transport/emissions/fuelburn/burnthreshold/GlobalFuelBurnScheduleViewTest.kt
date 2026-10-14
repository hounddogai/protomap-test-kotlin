package com.acme.transport.emissions.fuelburn.burnthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalFuelBurnScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalFuelBurnSchedule("code-459", "title-84")
        val view = toGlobalFuelBurnScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalFuelBurnScheduleViews(mutableListOf<GlobalFuelBurnSchedule>(item, item)).size, "views")
    }
}
