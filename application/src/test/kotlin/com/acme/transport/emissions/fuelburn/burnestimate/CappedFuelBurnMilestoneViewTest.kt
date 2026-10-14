package com.acme.transport.emissions.fuelburn.burnestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedFuelBurnMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedFuelBurnMilestone("code-249", "title-598")
        val view = toCappedFuelBurnMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedFuelBurnMilestoneViews(mutableListOf<CappedFuelBurnMilestone>(item, item)).size, "views")
    }
}
