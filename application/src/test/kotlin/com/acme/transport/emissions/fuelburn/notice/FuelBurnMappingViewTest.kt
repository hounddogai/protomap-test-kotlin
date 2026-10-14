package com.acme.transport.emissions.fuelburn.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnMapping("code-234", "title-853")
        val view = toFuelBurnMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnMappingViews(mutableListOf<FuelBurnMapping>(item, item)).size, "views")
    }
}
