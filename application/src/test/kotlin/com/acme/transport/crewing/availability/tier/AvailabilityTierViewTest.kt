package com.acme.transport.crewing.availability.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityTier("code-121", "label-386")
        val view = toAvailabilityTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityTierViews(mutableListOf<AvailabilityTier>(item, item)).size, "views")
    }
}
