package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyFuelBurnGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyFuelBurnGroup("code-308", "title-778")
        val view = toLegacyFuelBurnGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyFuelBurnGroupViews(mutableListOf<LegacyFuelBurnGroup>(item, item)).size, "views")
    }
}
