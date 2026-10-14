package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredFuelBurnRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredFuelBurnRollup("code-261", "label-807")
        val view = toTieredFuelBurnRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredFuelBurnRollupViews(mutableListOf<TieredFuelBurnRollup>(item, item)).size, "views")
    }
}
