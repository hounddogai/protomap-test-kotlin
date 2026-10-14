package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnDigest("code-615", "name-919")
        val view = toFuelBurnDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnDigestViews(mutableListOf<FuelBurnDigest>(item, item)).size, "views")
    }
}
