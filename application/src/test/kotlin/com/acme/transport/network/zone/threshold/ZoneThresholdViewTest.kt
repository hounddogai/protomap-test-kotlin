package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneThreshold("code-681", "label-941")
        val view = toZoneThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneThresholdViews(mutableListOf<ZoneThreshold>(item, item)).size, "views")
    }
}
