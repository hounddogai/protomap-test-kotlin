package com.acme.transport.emissions.fuelburn.group

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnQuota("code-563", "title-446")
        val view = toFuelBurnQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnQuotaViews(mutableListOf<FuelBurnQuota>(item, item)).size, "views")
    }
}
