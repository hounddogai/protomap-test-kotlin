package com.acme.transport.emissions.fuelburn.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnSchedule("code-254", "label-294")
        val view = toFuelBurnScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnScheduleViews(mutableListOf<FuelBurnSchedule>(item, item)).size, "views")
    }
}
