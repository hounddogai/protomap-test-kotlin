package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictFuelBurnGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictFuelBurnGroup("code-174", "label-353")
        val view = toStrictFuelBurnGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictFuelBurnGroupViews(mutableListOf<StrictFuelBurnGroup>(item, item)).size, "views")
    }
}
