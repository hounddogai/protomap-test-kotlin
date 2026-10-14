package com.acme.transport.customs.tariff.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffCycle("code-436", "label-598")
        val view = toTariffCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffCycleViews(mutableListOf<TariffCycle>(item, item)).size, "views")
    }
}
