package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredFuelBurnSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredFuelBurnSlot("code-481", "name-136")
        val view = toTieredFuelBurnSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredFuelBurnSlotViews(mutableListOf<TieredFuelBurnSlot>(item, item)).size, "views")
    }
}
