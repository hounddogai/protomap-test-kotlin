package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnTemplate("code-307", "name-701")
        val view = toFuelBurnTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnTemplateViews(mutableListOf<FuelBurnTemplate>(item, item)).size, "views")
    }
}
