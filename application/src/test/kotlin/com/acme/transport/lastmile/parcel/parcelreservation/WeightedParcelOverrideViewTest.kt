package com.acme.transport.lastmile.parcel.parcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedParcelOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedParcelOverride("code-607", "name-973")
        val view = toWeightedParcelOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedParcelOverrideViews(mutableListOf<WeightedParcelOverride>(item, item)).size, "views")
    }
}
