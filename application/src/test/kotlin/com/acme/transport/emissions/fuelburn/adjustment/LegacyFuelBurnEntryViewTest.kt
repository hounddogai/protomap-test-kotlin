package com.acme.transport.emissions.fuelburn.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyFuelBurnEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyFuelBurnEntry("code-16", "name-707")
        val view = toLegacyFuelBurnEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyFuelBurnEntryViews(mutableListOf<LegacyFuelBurnEntry>(item, item)).size, "views")
    }
}
