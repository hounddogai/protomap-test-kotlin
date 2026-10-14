package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackTariffCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackTariffCycle("code-939", "title-357")
        val view = toFallbackTariffCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackTariffCycleViews(mutableListOf<FallbackTariffCycle>(item, item)).size, "views")
    }
}
