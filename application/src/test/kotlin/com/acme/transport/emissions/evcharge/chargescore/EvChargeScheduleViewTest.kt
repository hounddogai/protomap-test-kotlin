package com.acme.transport.emissions.evcharge.chargescore

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeSchedule("code-405", "label-827")
        val view = toEvChargeScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeScheduleViews(mutableListOf<EvChargeSchedule>(item, item)).size, "views")
    }
}
