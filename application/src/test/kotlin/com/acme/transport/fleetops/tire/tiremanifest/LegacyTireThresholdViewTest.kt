package com.acme.transport.fleetops.tire.tiremanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTireThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTireThreshold("code-610", "label-539")
        val view = toLegacyTireThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTireThresholdViews(mutableListOf<LegacyTireThreshold>(item, item)).size, "views")
    }
}
