package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnRecord("code-610", "title-727")
        val view = toFuelBurnRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnRecordViews(mutableListOf<FuelBurnRecord>(item, item)).size, "views")
    }
}
