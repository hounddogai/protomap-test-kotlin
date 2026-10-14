package com.acme.transport.customs.tariff.request

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackTariffAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackTariffAllocation("code-66", "label-400")
        val view = toFallbackTariffAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackTariffAllocationViews(mutableListOf<FallbackTariffAllocation>(item, item)).size, "views")
    }
}
