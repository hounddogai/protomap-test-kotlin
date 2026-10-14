package com.acme.transport.customs.tariff.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffSchedule("code-919", "title-155")
        val view = toTariffScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffScheduleViews(mutableListOf<TariffSchedule>(item, item)).size, "views")
    }
}
