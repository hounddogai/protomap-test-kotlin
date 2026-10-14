package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffTier("code-114", "title-101")
        val view = toTariffTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffTierViews(mutableListOf<TariffTier>(item, item)).size, "views")
    }
}
