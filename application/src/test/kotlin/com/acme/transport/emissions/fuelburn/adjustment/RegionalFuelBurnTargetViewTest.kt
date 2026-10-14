package com.acme.transport.emissions.fuelburn.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalFuelBurnTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalFuelBurnTarget("code-676", "title-987")
        val view = toRegionalFuelBurnTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalFuelBurnTargetViews(mutableListOf<RegionalFuelBurnTarget>(item, item)).size, "views")
    }
}
