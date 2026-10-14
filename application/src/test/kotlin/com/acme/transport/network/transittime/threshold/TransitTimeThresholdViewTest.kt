package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeThreshold("code-603", "title-470")
        val view = toTransitTimeThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeThresholdViews(mutableListOf<TransitTimeThreshold>(item, item)).size, "views")
    }
}
