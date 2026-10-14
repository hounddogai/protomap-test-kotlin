package com.acme.transport.fleetops.tire.target

import kotlin.test.Test
import kotlin.test.assertEquals

class TireThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireThreshold("code-491", "title-627")
        val view = toTireThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireThresholdViews(mutableListOf<TireThreshold>(item, item)).size, "views")
    }
}
