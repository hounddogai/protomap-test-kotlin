package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualFuelBurnVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualFuelBurnVariance("code-35", "title-50")
        val view = toManualFuelBurnVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualFuelBurnVarianceViews(mutableListOf<ManualFuelBurnVariance>(item, item)).size, "views")
    }
}
