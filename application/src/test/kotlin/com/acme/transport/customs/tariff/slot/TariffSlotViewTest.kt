package com.acme.transport.customs.tariff.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffSlot("code-707", "label-499")
        val view = toTariffSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffSlotViews(mutableListOf<TariffSlot>(item, item)).size, "views")
    }
}
