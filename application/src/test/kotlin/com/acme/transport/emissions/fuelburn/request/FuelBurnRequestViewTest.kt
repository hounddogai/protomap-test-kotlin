package com.acme.transport.emissions.fuelburn.request

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnRequest("code-875", "name-276")
        val view = toFuelBurnRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnRequestViews(mutableListOf<FuelBurnRequest>(item, item)).size, "views")
    }
}
