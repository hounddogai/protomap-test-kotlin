package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingFuelBurnWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingFuelBurnWindow("code-385", "title-388")
        val view = toPendingFuelBurnWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingFuelBurnWindowViews(mutableListOf<PendingFuelBurnWindow>(item, item)).size, "views")
    }
}
