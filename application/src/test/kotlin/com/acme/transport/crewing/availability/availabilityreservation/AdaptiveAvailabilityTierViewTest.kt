package com.acme.transport.crewing.availability.availabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveAvailabilityTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveAvailabilityTier("code-712", "title-988")
        val view = toAdaptiveAvailabilityTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveAvailabilityTierViews(mutableListOf<AdaptiveAvailabilityTier>(item, item)).size, "views")
    }
}
