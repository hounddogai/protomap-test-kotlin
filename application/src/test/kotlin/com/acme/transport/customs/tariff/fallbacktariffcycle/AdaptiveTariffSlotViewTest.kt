package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTariffSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTariffSlot("code-674", "title-474")
        val view = toAdaptiveTariffSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTariffSlotViews(mutableListOf<AdaptiveTariffSlot>(item, item)).size, "views")
    }
}
