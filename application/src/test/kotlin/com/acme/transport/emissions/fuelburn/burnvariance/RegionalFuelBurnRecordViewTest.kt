package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalFuelBurnRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalFuelBurnRecord("code-898", "title-846")
        val view = toRegionalFuelBurnRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalFuelBurnRecordViews(mutableListOf<RegionalFuelBurnRecord>(item, item)).size, "views")
    }
}
