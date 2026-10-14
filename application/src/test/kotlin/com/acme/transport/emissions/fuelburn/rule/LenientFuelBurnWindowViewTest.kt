package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientFuelBurnWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientFuelBurnWindow("code-620", "title-111")
        val view = toLenientFuelBurnWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientFuelBurnWindowViews(mutableListOf<LenientFuelBurnWindow>(item, item)).size, "views")
    }
}
