package com.acme.transport.crewing.availability.primaryavailabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientAvailabilityTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientAvailabilityTier("code-745", "name-495")
        val view = toLenientAvailabilityTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientAvailabilityTierViews(mutableListOf<LenientAvailabilityTier>(item, item)).size, "views")
    }
}
