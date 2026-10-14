package com.acme.transport.customs.tariff.tariffprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTariffAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTariffAllocation("code-366", "name-803")
        val view = toLegacyTariffAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTariffAllocationViews(mutableListOf<LegacyTariffAllocation>(item, item)).size, "views")
    }
}
