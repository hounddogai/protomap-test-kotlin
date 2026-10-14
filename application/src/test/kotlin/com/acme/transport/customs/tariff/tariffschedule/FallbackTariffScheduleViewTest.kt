package com.acme.transport.customs.tariff.tariffschedule

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackTariffScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackTariffSchedule("code-840", "name-157")
        val view = toFallbackTariffScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackTariffScheduleViews(mutableListOf<FallbackTariffSchedule>(item, item)).size, "views")
    }
}
