package com.acme.transport.customs.tariff.tariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTariffCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTariffCycle("code-670", "title-209")
        val view = toStrictTariffCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTariffCycleViews(mutableListOf<StrictTariffCycle>(item, item)).size, "views")
    }
}
