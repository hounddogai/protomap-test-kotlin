package com.acme.transport.emissions.fuelburn.group

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnGroup("code-161", "name-833")
        val view = toFuelBurnGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnGroupViews(mutableListOf<FuelBurnGroup>(item, item)).size, "views")
    }
}
